package neo.requirements.cplex;

import neo.requirements.util.EfficientSolution;

public interface ILPSolverListener {
	public void notifyEfficientSolutionFound(EfficientSolution solution);
	public void info(String key, String value);
	public void reportInnecesaryRunOfSolver();

}
