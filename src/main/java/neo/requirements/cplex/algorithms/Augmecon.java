package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.List;
import java.util.Properties;

import neo.requirements.cplex.ILPAdaptor;
import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.EfficientSolutionWithTimeStamp;
import neo.requirements.util.SingleThreadCPUTimer;

public class Augmecon extends AbstractILPBasedBIobjectiveSolver {
	
	private Double lambdaValue= null;

	@Override
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor) {
		SingleThreadCPUTimer timer = new SingleThreadCPUTimer();
		timer.startTimer();
		try {
			createParetoFront();

			configureOrderForObjectives(adaptor);
			
			Modelo modelo = adaptor.ilpModelForConstraints();
			modelo.cplex.addMinimize(adaptor.getObjective(firstObjective()));
			
			EfficientSolution previousEfficientSolution=null;
			IloNumVar ell;

			while (solveIlpInstance(modelo)) {
				double firstObjValue = (int)Math.round(evaluateLinearExpression(modelo, adaptor.getObjective(firstObjective())));
				double secondObjValue = (int)Math.round(evaluateLinearExpression(modelo, adaptor.getObjective(secondObjective())));
				
				EfficientSolution currentEfficientSolution=buildEfficientSolution(timer, firstObjValue, secondObjValue);
				
				if (!dominates(currentEfficientSolution, previousEfficientSolution)) {
					reportEfficientSolution(previousEfficientSolution);
				}
				previousEfficientSolution=currentEfficientSolution;
				
				double lambda;
				if (lambdaValue != null) {
					lambda = lambdaValue;
				} else {
					lambda=adaptor.minimumDifferenceBetweenEfficientSolutions(firstObjective())/
						(secondObjValue-adaptor.idealLowerBound(secondObjective()));
					reportLambda(lambda);
				}
				
				modelo = adaptor.ilpModelForConstraints();
				IloLinearNumExpr objective = adaptor.getObjective(firstObjective());
				ell = modelo.cplex.numVar(0, Double.POSITIVE_INFINITY);
				objective.addTerm(-lambda, ell);
				modelo.cplex.addMinimize(objective);
				IloLinearNumExpr secondObjective = adaptor.getObjective(secondObjective());
				secondObjective.addTerm(1.0, ell);
				modelo.cplex.addEq(secondObjective, secondObjValue-step);
			}
			reportEfficientSolution(previousEfficientSolution);
			
			return paretoFront;
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}

	protected EfficientSolutionWithTimeStamp buildEfficientSolution(
			SingleThreadCPUTimer timer, double firstObjValue,
			double secondObjValue) {
		
		double[] values = new double [2];
		values[firstObjective()] = firstObjValue;
		values[secondObjective()] = secondObjValue;
		return new EfficientSolutionWithTimeStamp(values,  timer.elapsedTimeInMilliseconds());
	}

	protected void reportLambda(double lambda) {
		if (listener != null) {
			listener.info("lambda", ""+lambda);
		}
	}

	private double evaluateLinearExpression(Modelo modelo, IloLinearNumExpr objective) throws IloException {
		double result = 0.0;
		for (IloLinearNumExprIterator iterator = objective.linearIterator(); iterator.hasNext();) {
			IloNumVar variable = iterator.nextNumVar();
			double coefficient = iterator.getValue();
			
			try {
				result += coefficient * modelo.cplex.getValue(variable);
			} catch (UnknownObjectException e) {
				result += Math.min(0, coefficient);
			}
		}
		return result;
	}

	private boolean dominates(EfficientSolution currentEfficientSolution,
			EfficientSolution previousEfficientSolution) {
		if (previousEfficientSolution==null) {
			return true;
		}
		
		boolean oneSmaller = false;
		for (int objective=0; objective < currentEfficientSolution.getNumberOfObjectives(); objective++) {
			if (currentEfficientSolution.getObjectiveValue(objective) < previousEfficientSolution.getObjectiveValue(objective)) {
				oneSmaller = true;
			} else if (currentEfficientSolution.getObjectiveValue(objective) > previousEfficientSolution.getObjectiveValue(objective)) {
				return false;
			}
		}
		return oneSmaller;
	}

	@Override
	public String getName() {
		return "Augmecon";
	}

	@Override
	public String getCommandLineName() {
		return "augmecon";
	}

	@Override
	public void setConfiguration(Properties configuration) {
		super.setConfiguration(configuration);
		configureLambda(configuration);
	}

	protected void configureLambda(Properties configuration) {
		String lambdaProperty= configuration.getProperty("lambda");
		if (lambdaProperty!=null) {
			if ("adaptive".equals(lambdaProperty)) {
				lambdaValue=null;
			} else {
				lambdaValue = Double.parseDouble(lambdaProperty);
			}
		}
	}
	
	

}
