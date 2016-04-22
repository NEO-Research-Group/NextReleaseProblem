package neo.requirements.sat.cplex;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;

public interface ILPAdaptor {
	public Modelo ilpModelForConstraints()  throws IloException;
	public IloLinearNumExpr firstObjective()  throws IloException;
	public IloLinearNumExpr secondObjective() throws IloException;
}
