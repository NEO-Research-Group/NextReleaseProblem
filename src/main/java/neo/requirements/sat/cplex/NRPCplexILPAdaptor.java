package neo.requirements.sat.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.cplex.IloCplex;
import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.NextReleaseProblem.Constraint;
import neo.requirements.sat.NextReleaseProblem.KindOfInstance;

public class NRPCplexILPAdaptor implements ILPAdaptor {
	
	private Modelo modelo;
	private NextReleaseProblem nextReleaseProblem;

	public NRPCplexILPAdaptor(NextReleaseProblem nrp) {
		nextReleaseProblem=nrp;
	}
	
	@Override
	public Modelo ilpModelForConstraints()  throws IloException  {
		clearModelo();
		computeRequirementConstraints();
		computeStakeholderConstraints();
		return modelo;
	}

	@Override
	public IloLinearNumExpr firstObjective() throws IloException {
		if (modelo == null) {
			throw new RuntimeException("No model in the adaptor, please call ilpModelForConstraint before");
		}
		return computeEffortExpression();
	}

	@Override
	public IloLinearNumExpr secondObjective()  throws IloException  {
		if (modelo == null) {
			throw new RuntimeException("No model in the adaptor, please call ilpModelForConstraint before");
		}
		return computeValueExpression(-1);
	}

	private void clearModelo() throws IloException {
		switch (nextReleaseProblem.getKindOfInstance()) {
		case ALMERIA:
			clearModeloAlmeria();
			break;
		case XUAN:
			clearModeloXuan();
			break;
		default:
			throw new RuntimeException("Unsupported kind of instance: "+nextReleaseProblem.getKindOfInstance());
		}
	}
	
	private IloLinearNumExpr computeValueExpression(int multiplier) throws IloException {
		switch (nextReleaseProblem.getKindOfInstance()) {
		case ALMERIA:
			return computeValueExpressionAlmeria(multiplier);
		case XUAN:
			return computeValueExpressionXuan(multiplier);
		default:
			throw new RuntimeException("Unsupported kind of instance: "+nextReleaseProblem.getKindOfInstance());
		}
	}
	
	private void clearModeloXuan() throws IloException {
		modelo = new Modelo();
		modelo.cplex = new IloCplex();
		modelo.variables = modelo.cplex.boolVarArray(nextReleaseProblem.getRequirements()+nextReleaseProblem.getStakeholders());
		int requirement;
		for (requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			getRequirementVariable(requirement).setName(getRequirementName(requirement));
		}
		
		for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
			modelo.variables[requirement++].setName(getStakeholderName(stakeholder));
		}
	}
	
	private void clearModeloAlmeria() throws IloException {
		modelo = new Modelo();
		modelo.cplex = new IloCplex();
		modelo.variables = modelo.cplex.boolVarArray(nextReleaseProblem.getRequirements());
		int requirement;
		for (requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			getRequirementVariable(requirement).setName(getRequirementName(requirement));
		}
	}

	protected String getStakeholderName(int stakeholder) {
		return "s"+(stakeholder+1);
	}

	protected String getRequirementName(int requirement) {
		return "r"+(requirement+1);
	}
	
	private IloLinearNumExpr computeEffortExpression() throws IloException {
			IloLinearNumExpr effortExpression = modelo.cplex.linearNumExpr();
			for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
				effortExpression.addTerm(nextReleaseProblem.getEffortOfRequirement(requirement), getRequirementVariable(requirement));
			}
			return effortExpression;
	}

	protected IloIntVar getRequirementVariable(int requirement) {
		return modelo.variables[requirement];
	}
	
	private IloLinearNumExpr computeValueExpressionXuan(int multiplier) throws IloException {
		IloLinearNumExpr valueExpression = modelo.cplex.linearNumExpr();
		for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
			valueExpression.addTerm(multiplier*nextReleaseProblem.getWeightOfStakeholder(stakeholder), getStakeholderVariable(stakeholder));
		}
		return valueExpression;
	}
	
	private IloLinearNumExpr computeValueExpressionAlmeria(int multiplier) throws IloException {
		IloLinearNumExpr valueExpression = modelo.cplex.linearNumExpr();
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			valueExpression.addTerm(multiplier*nextReleaseProblem.totalValueForRequirement(requirement), modelo.variables[requirement]);
		}
		return valueExpression;
	}

	protected IloIntVar getStakeholderVariable(int stakeholder) {
		return modelo.variables[nextReleaseProblem.getRequirements()+stakeholder];
	}

	private void computeRequirementConstraints() throws IloException {
		for (Constraint constraint: nextReleaseProblem.getConstraints()) {
			linearExpressionForConstraint(constraint);
		}
	}
	
	private void computeStakeholderConstraints() throws IloException {
		if (!KindOfInstance.XUAN.equals(nextReleaseProblem.getKindOfInstance())) {
			return;
		}
		
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
				if (nextReleaseProblem.getValueOfRequirementForStakeholder(requirement, stakeholder) > 0) {
					modelo.cplex.addGe(getRequirementVariable(requirement), getStakeholderVariable(stakeholder));
				}
			}
		}
	}
	
	private void linearExpressionForConstraint(Constraint constraint) throws IloException {		
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