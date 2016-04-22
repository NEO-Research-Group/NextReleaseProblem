package neo.requirements.cplex.old;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import neo.requirements.cplex.Modelo;
import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.NextReleaseProblem.Constraint;

public class XuanNRPToCplexAdaptor implements CplexAdaptor {
	
	private Modelo modelo;
	private NextReleaseProblem nextReleaseProblem;

	public XuanNRPToCplexAdaptor(NextReleaseProblem nrp) {
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
		modelo.variables = modelo.cplex.boolVarArray(nextReleaseProblem.getRequirements()+nextReleaseProblem.getStakeholders());
		int variable;
		for (variable=0; variable < nextReleaseProblem.getRequirements(); variable++) {
			getRequirementVariable(variable).setName(getRequirementName(variable));
		}
		
		for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
			modelo.variables[variable++].setName(getStakeholderName(stakeholder));
		}
	}

	protected String getStakeholderName(int stakeholder) {
		return "s"+(stakeholder+1);
	}

	protected String getRequirementName(int requirement) {
		return "x"+(requirement+1);
	}
	
	
	@Override
	public Modelo ilpInstanceForMinimizingEffort (int valueLowerBound) {
		try {
			clearModelo();
			computeIlpConstraints();
			computeStakeholderConstraints();
			modelo.cplex.addGe(computeValueExpression(1), valueLowerBound);
			modelo.cplex.addMinimize(computeEffortExpression());
			return modelo;
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}

	@Override
	public Modelo ilpInstanceForMaximizingValue (int effortUpperBound) {
		try {
			clearModelo();
			computeIlpConstraints();
			computeStakeholderConstraints();
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
				effortExpression.addTerm(nextReleaseProblem.getEffortOfRequirement(requirement), getRequirementVariable(requirement));
			}
			return effortExpression;
	}

	protected IloIntVar getRequirementVariable(int requirement) {
		return modelo.variables[requirement];
	}
	
	private IloNumExpr computeValueExpression(int multiplier) throws IloException {
		IloLinearNumExpr valueExpression = modelo.cplex.linearNumExpr();
		for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
			valueExpression.addTerm(multiplier*nextReleaseProblem.getWeightOfStakeholder(stakeholder), getStakeholderVariable(stakeholder));
		}
		return valueExpression;
	}

	protected IloIntVar getStakeholderVariable(int stakeholder) {
		return modelo.variables[nextReleaseProblem.getRequirements()+stakeholder];
	}

	private void computeIlpConstraints() throws IloException {
		for (Constraint constraint: nextReleaseProblem.getConstraints()) {
			ilpConstraintsForConstraint(constraint);
		}
	}
	
	private void computeStakeholderConstraints() throws IloException {
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
				if (nextReleaseProblem.getValueOfRequirementForStakeholder(requirement, stakeholder) > 0) {
					modelo.cplex.addGe(getRequirementVariable(requirement), getStakeholderVariable(stakeholder));
				}
			}
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
		expr.addTerm(1, getRequirementVariable(firstRequirement));
		expr.addTerm(1, getRequirementVariable(secondRequirement));
		modelo.cplex.addLe(expr, 1);
	}

	private void implicationConstraintToIlp(int firstRequirement, int secondRequirement)  throws IloException {
		modelo.cplex.addGe(getRequirementVariable(secondRequirement), getRequirementVariable(firstRequirement));
	}
	
	private void simultaneousConstraintToIlp(int firstRequirement, int secondRequirement) throws IloException {
		modelo.cplex.addEq(getRequirementVariable(firstRequirement), getRequirementVariable(secondRequirement));
	}

	
}