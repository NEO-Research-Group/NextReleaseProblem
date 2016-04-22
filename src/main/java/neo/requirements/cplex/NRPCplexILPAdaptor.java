package neo.requirements.cplex;

import ilog.concert.IloException;
import ilog.concert.IloIntVar;
import ilog.concert.IloLinearNumExpr;
import ilog.cplex.IloCplex;
import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.NextReleaseProblem.Constraint;
import neo.requirements.problems.NextReleaseProblem.KindOfInstance;

public class NRPCplexILPAdaptor implements ILPAdaptor {
	
	private static final int VALUE_OBJECTIVE = 1;
	private static final int EFFORT_OBJECTIVE = 0;
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
		ensureModeloExistsAndClear();
		modelo.variables = modelo.cplex.boolVarArray(nextReleaseProblem.getRequirements()+nextReleaseProblem.getStakeholders());
		int requirement;
		for (requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			getRequirementVariable(requirement).setName(getRequirementName(requirement));
		}
		
		for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
			modelo.variables[requirement++].setName(getStakeholderName(stakeholder));
		}
	}

	protected void ensureModeloExistsAndClear() throws IloException {
		if (modelo==null) {
			createModelo();
		} else {
			modelo.cplex.clearModel();
		}
	}

	private void createModelo() throws IloException {
		modelo = new Modelo();
		modelo.cplex = new IloCplex();
	}
	
	private void clearModeloAlmeria() throws IloException {
		ensureModeloExistsAndClear();
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

	@Override
	public IloLinearNumExpr getObjective(int objective) throws IloException {
		if (modelo == null) {
			throw new RuntimeException("No model in the adaptor, please call ilpModelForConstraint before");
		}
		
		if (objective==EFFORT_OBJECTIVE) {
			return computeEffortExpression();
		} else if (objective==VALUE_OBJECTIVE) {
			return computeValueExpression(-1);
		} else {
			throw new IllegalArgumentException("The number of objective must be 0 or 1 (biobjective problems)");
		}
	}

	@Override
	public double getNadirUpperBound(int objective) {
		if (objective==EFFORT_OBJECTIVE) {
			return nextReleaseProblem.sumOfAllRequirementsEffort();
		} else if (objective==VALUE_OBJECTIVE) {
			return computeMaximumSatisfaction();
		} else {
			throw new IllegalArgumentException("The number of objective must be 0 or 1 (biobjective problems)");
		}
	}

	private double computeMaximumSatisfaction() {
		switch (nextReleaseProblem.getKindOfInstance()) {
		case ALMERIA:
			return computeMaximumSatisfactionAlmeria();
		case XUAN:
			return computeMaximumSatisfactionXuan();
		default:
			throw new RuntimeException("Unsupported kind of instance: "+nextReleaseProblem.getKindOfInstance());
		}
	}

	private double computeMaximumSatisfactionAlmeria() {
		int sumSatisfaction=0;
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			sumSatisfaction += nextReleaseProblem.totalValueForRequirement(requirement);
		}
		return sumSatisfaction;
	}
	
	private double computeMaximumSatisfactionXuan() {
		int sumSatisfaction=0;
		for (int stakeholder=0; stakeholder < nextReleaseProblem.getStakeholders(); stakeholder++) {
			sumSatisfaction += nextReleaseProblem.getWeightOfStakeholder(stakeholder);
		}
		return sumSatisfaction;
	}

	
}