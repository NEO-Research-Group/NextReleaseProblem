package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;

public class AnytimeTchebycheff extends AbstractAnytime {

	protected Double lambdaValue = null;

	@Override
	public String getName() {
		return "Anytime Tchebycheff";
	}

	@Override
	public String getCommandLineName() {
		return "anytime-tchebycheff";
	}

	protected EfficientSolution getNonDominatedSolutionInRectangle(PairOfEfficientSolutions pair)
			throws IloException {

		Modelo modelo = adaptor.ilpModelForConstraints();
		double x0 = Math.min(pair.lower.getObjectiveValue(firstObjective()), 
				pair.upper.getObjectiveValue(firstObjective()));
		double x2 = Math.max(pair.lower.getObjectiveValue(firstObjective()), 
				pair.upper.getObjectiveValue(firstObjective()));
		
		double y0 = Math.max(pair.lower.getObjectiveValue(secondObjective()), 
				pair.upper.getObjectiveValue(secondObjective()));
		double y2 = Math.min(pair.lower.getObjectiveValue(secondObjective()), 
				pair.upper.getObjectiveValue(secondObjective()));
		
		double x1 = x2 - 0.5;
		double y1 = y0 - 0.5;
		
		modelo.cplex.addLe(adaptor.getObjective(firstObjective()), x2);
		modelo.cplex.addLe(adaptor.getObjective(secondObjective()), y0);
		
		IloNumVar z = modelo.cplex.numVar(Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
		IloLinearNumExpr x = adaptor.getObjective(firstObjective());
		IloLinearNumExpr y = adaptor.getObjective(secondObjective());
		
		IloNumExpr expr = modelo.cplex.sum(modelo.cplex.prod(x1-x0, y), modelo.cplex.prod(y0-y1, x));
		expr = modelo.cplex.sum(expr, (y1-y0)*x1-(x1-x0)*y1);
		modelo.cplex.addLe(expr, z);
		
		expr = modelo.cplex.sum(modelo.cplex.prod(x2-x1, y), modelo.cplex.prod(y1-y2, x));
		expr = modelo.cplex.sum(expr, (y2-y1)*x1-(x2-x1)*y1);
		modelo.cplex.addLe(expr, z);
		
		modelo.cplex.addMinimize(z);
				
		solveIlpInstance(modelo);
		double firstObjValue = (int)Math.round(evaluateLinearExpression(modelo, adaptor.getObjective(firstObjective())));
		double secondObjValue = (int)Math.round(evaluateLinearExpression(modelo, adaptor.getObjective(secondObjective())));

		return buildEfficientSolution(timer, firstObjValue, secondObjValue);
	}

	protected void addNewPairIfRequired(PairOfEfficientSolutions pair) {
		// Nothing to do
	}

}
