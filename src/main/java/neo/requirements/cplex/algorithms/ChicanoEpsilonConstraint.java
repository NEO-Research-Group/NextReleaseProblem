package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

import neo.requirements.cplex.ILPAdaptor;
import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.ILPSolverListener;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.EfficientSolutionWithTimeStamp;
import neo.requirements.util.SingleThreadCPUTimer;

public class ChicanoEpsilonConstraint implements ILPBasedBiobjectiveSolver {

	private static final int VALUE_OBJECTIVE = 1;
	private static final int EFFORT_OBJECTIVE = 0;
	
	private ILPSolverListener listener;

	@Override
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor) {
		SingleThreadCPUTimer timer = new SingleThreadCPUTimer();
		timer.startTimer();
		try {
			List<EfficientSolution> paretoFront = new ArrayList<>();

			Modelo modelo = adaptor.ilpModelForConstraints();
			modelo.cplex.addMinimize(adaptor.getObjective(VALUE_OBJECTIVE));

			while (solveIlpInstance(modelo)) {
				double value = (int)Math.round(modelo.cplex.getObjValue());

				modelo = adaptor.ilpModelForConstraints();
				modelo.cplex.addLe(adaptor.getObjective(VALUE_OBJECTIVE), value);
				modelo.cplex.addMinimize(adaptor.getObjective(EFFORT_OBJECTIVE));

				solveIlpInstance(modelo);

				double effort = (int)Math.round(modelo.cplex.getObjValue());

				EfficientSolution efficientSolution = new EfficientSolutionWithTimeStamp(new double [] {effort, -value},  timer.elapsedTimeInMilliseconds());
				reportEfficientSolution(efficientSolution);
				paretoFront.add(efficientSolution);

				modelo = adaptor.ilpModelForConstraints();
				modelo.cplex.addLe(adaptor.getObjective(EFFORT_OBJECTIVE), effort-1);
				modelo.cplex.addMinimize(adaptor.getObjective(VALUE_OBJECTIVE));
			}
			return paretoFront;
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}
	
	private void reportEfficientSolution(EfficientSolution efficientSolution) {
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
		return "Chicano epsilon constraint";
	}

}
