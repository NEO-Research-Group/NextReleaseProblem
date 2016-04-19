package neo.requirements.sat.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.NextReleaseProblem.Constraint;

public class NextReleaseToCplexAdaptor {
	
	private Modelo modelo;
	private NextReleaseProblem nextReleaseProblem;

	public NextReleaseToCplexAdaptor(NextReleaseProblem nrp) {
		nextReleaseProblem=nrp;
		modelo = new Modelo();
		try {
			modelo.cplex = new IloCplex();
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}
	
	private void clearModelo() throws IloException {
		modelo.cplex.clearModel();
		modelo.variables = modelo.cplex.boolVarArray(nextReleaseProblem.getRequirements());
		int requirement=0;
		for (IloIntVar variable: modelo.variables) {
			variable.setName("x"+(++requirement));
		}
	}
	
	
	public Modelo ilpInstanceForMinimizingEffort (int valueLowerBound) {
		try {
			clearModelo();
			computeIlpConstraints();
			modelo.cplex.addGe(computeValueExpression(1), valueLowerBound);
			modelo.cplex.addMinimize(computeEffortExpression());
			return modelo;
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}

	public Modelo ilpInstanceForMaximizingValue (int effortUpperBound) {
		try {
			clearModelo();
			computeIlpConstraints();
			modelo.cplex.addLe(computeEffortExpression(), effortUpperBound);
			modelo.cplex.addMaximize(computeValueExpression(1));
			return modelo;
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}

	private IloNumExpr computeEffortExpression() throws IloException {
			IloLinearNumExpr effortExpression = modelo.cplex.linearNumExpr();
			for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
				effortExpression.addTerm(nextReleaseProblem.getEffortOfRequirement(requirement), modelo.variables[requirement]);
			}
			return effortExpression;
	}
	
	private IloNumExpr computeValueExpression(int multiplier) throws IloException {

		IloLinearNumExpr valueExpression = modelo.cplex.linearNumExpr();
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			valueExpression.addTerm(multiplier*nextReleaseProblem.totalValueForRequirement(requirement), modelo.variables[requirement]);
		}
		return valueExpression;
	}

	private void computeIlpConstraints() throws IloException {
		for (Constraint constraint: nextReleaseProblem.getConstraints()) {
			ilpConstraintsForConstraint(constraint);
		}
	}
	
	private void ilpConstraintsForConstraint(Constraint constraint) throws IloException {		
		switch (constraint.type) {
			case IMPLICATION:
				implicationConstraintToIlp(constraint.firstRequirement, constraint.secondRequirement);
				break;
			case EXCLUSION:
				exclusionConstraintToIlp(constraint.firstRequirement, constraint.secondRequirement);
				break;
			case SIMULTANEOUS:
				simultaneousConstraintToIlp(constraint.firstRequirement, constraint.secondRequirement);
				break;
		}		
	}

	private void exclusionConstraintToIlp(int firstRequirement, int secondRequirement) throws IloException {
		IloLinearNumExpr expr = modelo.cplex.linearNumExpr();
		expr.addTerm(1, modelo.variables[firstRequirement]);
		expr.addTerm(1, modelo.variables[secondRequirement]);
		modelo.cplex.addLe(expr, 1);
	}

	private void implicationConstraintToIlp(int firstRequirement, int secondRequirement)  throws IloException {
		modelo.cplex.addGe(modelo.variables[secondRequirement], modelo.variables[firstRequirement]);
	}
	
	private void simultaneousConstraintToIlp(int firstRequirement, int secondRequirement) throws IloException {
		modelo.cplex.addEq(modelo.variables[firstRequirement], modelo.variables[secondRequirement]);
	}

	
}