package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.List;

import neo.requirements.cplex.ILPAdaptor;
import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.EfficientSolutionWithTimeStamp;
import neo.requirements.util.SingleThreadCPUTimer;

public class VeerapenEpsilonConstraint extends AbstractILPBasedBIobjectiveSolver {

	@Override
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor) {
		SingleThreadCPUTimer timer = new SingleThreadCPUTimer();
		timer.startTimer();
		setTimerStop(timer);
		try {
			createParetoFront();
			configureOrderForObjectives(adaptor);

			Modelo modelo = adaptor.ilpModelForConstraints();
			modelo.cplex.addMinimize(adaptor.getObjective(firstObjective()));
			
			EfficientSolution previousEfficientSolution=null;

			while (!timer.shouldStop() && solveIlpInstance(modelo)) {
				double firstObjValue = (int)Math.round(modelo.cplex.getObjValue());
				double secondObjValue = (int)Math.round(evaluateLinearExpression(modelo, adaptor.getObjective(secondObjective())));
				
				EfficientSolution currentEfficientSolution=buildEfficientSolution(timer, firstObjValue, secondObjValue);
				
				if (!dominates(currentEfficientSolution, previousEfficientSolution)) {
					reportEfficientSolution(previousEfficientSolution);
				}
				previousEfficientSolution=currentEfficientSolution;
				
				modelo = adaptor.ilpModelForConstraints();
				modelo.cplex.addLe(adaptor.getObjective(secondObjective()), secondObjValue-step);
				modelo.cplex.addMinimize(adaptor.getObjective(firstObjective()));
			}
			reportEfficientSolution(previousEfficientSolution);
			
			return paretoFront;
		} catch (IloException e) {
			throw new RuntimeException (e);
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
		return "Veerapen epsilon constraint";
	}

	@Override
	public String getCommandLineName() {
		return "veerapen";
	}

}
