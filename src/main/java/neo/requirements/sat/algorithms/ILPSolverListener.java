package neo.requirements.sat.algorithms;

import neo.requirements.sat.util.EfficientSolution;

public interface ILPSolverListener {
	public void notifyEfficientSolutionFound(EfficientSolution solution);

}
