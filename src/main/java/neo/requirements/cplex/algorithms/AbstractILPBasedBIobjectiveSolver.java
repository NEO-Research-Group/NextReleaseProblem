package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;

import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.ILPSolverListener;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;

public abstract class AbstractILPBasedBIobjectiveSolver  implements ILPBasedBiobjectiveSolver{

	protected ILPSolverListener listener;
	protected List<EfficientSolution> paretoFront;

	public AbstractILPBasedBIobjectiveSolver() {
		super();
	}

	protected void reportEfficientSolution(EfficientSolution previousEfficientSolution) {
		notifyEfficientSolution(previousEfficientSolution);
		paretoFront.add(previousEfficientSolution);
	}

	private void notifyEfficientSolution(EfficientSolution efficientSolution) {
		if (listener != null) {
			listener.notifyEfficientSolutionFound(efficientSolution);
		}
		
	}

	protected boolean solveIlpInstance(Modelo modelo) throws IloException {	
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

	protected void createParetoFront() {
		paretoFront = new ArrayList<>();
	}

}