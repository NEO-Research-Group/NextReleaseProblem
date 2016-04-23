package neo.requirements.cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;

public interface ILPAdaptor {
	public Modelo ilpModelForConstraints()  throws IloException;
	public IloLinearNumExpr getObjective(int objective) throws IloException;
	public double minimumDifferenceBetweenEfficientSolutions(int objective);
	public double idealLowerBound(int objective);
	public double nadirUpperBound(int objective);
}
