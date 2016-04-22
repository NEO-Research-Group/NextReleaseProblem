package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.ArrayList;
import java.util.List;

import neo.requirements.cplex.ILPAdaptor;
import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.ILPSolverListener;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.EfficientSolutionWithTimeStamp;
import neo.requirements.util.SingleThreadCPUTimer;

public class VeerapenEpsilonConstraint implements ILPBasedBiobjectiveSolver {

	private static final int VALUE_OBJECTIVE = 1;
	private static final int EFFORT_OBJECTIVE = 0;
	
	private ILPSolverListener listener;
	private List<EfficientSolution> paretoFront;

	@Override
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor) {
		SingleThreadCPUTimer timer = new SingleThreadCPUTimer();
		timer.startTimer();
		try {
			paretoFront = new ArrayList<>();

			Modelo modelo = adaptor.ilpModelForConstraints();
			modelo.cplex.addMinimize(adaptor.getObjective(VALUE_OBJECTIVE));
			
			EfficientSolution previousEfficientSolution=null;

			while (solveIlpInstance(modelo)) {
				double value = (int)Math.round(modelo.cplex.getObjValue());
				double effort = (int)Math.round(evaluateLinearExpression(modelo, adaptor.getObjective(EFFORT_OBJECTIVE)));
				
				EfficientSolution currentEfficientSolution=new EfficientSolutionWithTimeStamp(new double [] {effort, value},  timer.elapsedTimeInMilliseconds());
				
				if (!dominates(currentEfficientSolution, previousEfficientSolution)) {
					reportEfficientSolution(previousEfficientSolution);
				}
				previousEfficientSolution=currentEfficientSolution;
				
				modelo = adaptor.ilpModelForConstraints();
				modelo.cplex.addLe(adaptor.getObjective(EFFORT_OBJECTIVE), effort-1);
				modelo.cplex.addMinimize(adaptor.getObjective(VALUE_OBJECTIVE));
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

	protected void reportEfficientSolution(
			EfficientSolution previousEfficientSolution) {
		notifyEfficientSolution(previousEfficientSolution);
		paretoFront.add(previousEfficientSolution);
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

	private void notifyEfficientSolution(EfficientSolution efficientSolution) {
		if (listener != null) {
			listener.notifyEfficientSolutionFound(efficientSolution);
		}
		
	}

	public boolean solveIlpInstance(Modelo modelo) throws IloException {	
			modelo.cplex.setOut(null);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpInt, 1E-9);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpGap, 1E-9);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpOpt, 1E-9);
			return modelo.cplex.solve();
	}

	@Override
	public void setListener(ILPSolverListener listener) {
		this.listener = listener;
	}
	
	@Override
	public String getName() {
		return "Veerapen epsilon constraint";
	}

}
