package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.concert.IloLinearNumExpr;
import ilog.concert.IloLinearNumExprIterator;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex.UnknownObjectException;

import java.util.List;
import java.util.PriorityQueue;

import neo.requirements.cplex.ILPAdaptor;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.EfficientSolutionWithTimeStamp;
import neo.requirements.util.SingleThreadCPUTimer;


public abstract class AbstractAnytime extends AbstractILPBasedBIobjectiveSolver {
	
	public static class PairOfEfficientSolutions implements Comparable<PairOfEfficientSolutions>, Cloneable{
		public EfficientSolution lower;
		public EfficientSolution upper;
		
		public PairOfEfficientSolutions(EfficientSolution lower, EfficientSolution upper) {
			this.lower = lower;
			this.upper = upper;
		}
		
		public double hypervolume() {
			double hv = 1.0;
			for (int i=0; i < lower.getNumberOfObjectives(); i++) {
				hv *= spanInObjective(i);
			}
			return hv;
		}

		public double spanInObjective(int objective) {
			return Math.abs(upper.getObjectiveValue(objective) - lower.getObjectiveValue(objective));
		}
		
		public boolean isStrictlyIncludedInRegion(EfficientSolution solution) {
			for (int i=0; i < solution.getNumberOfObjectives(); i++) {
				if (!isStrinctlyInsideTheInterval(solution.getObjectiveValue(i), lower.getObjectiveValue(i), upper.getObjectiveValue(i))) {
					return false;
				}
			}
			return true;
		}
		
		private boolean isStrinctlyInsideTheInterval(double x, double a, double b) {
			if (b < a) {
				double tmp=a;
				a=b;
				b=tmp;
			}
			return (a < x) && (x < b);
		}
		
		@Override
		public int compareTo(PairOfEfficientSolutions o) {
			return Double.compare(o.hypervolume(), this.hypervolume());
		}

		@Override
		public PairOfEfficientSolutions clone(){
			PairOfEfficientSolutions obj = null;
			try {
				obj = (PairOfEfficientSolutions) super.clone();
			} catch (CloneNotSupportedException e) {
				
			}
			return obj;
		}
		
		public String toString() {
			return "[("+lower+"), ("+upper+")]";
		}
	}
	
	protected PriorityQueue<PairOfEfficientSolutions> queue;
	protected SingleThreadCPUTimer timer;
	protected ILPAdaptor adaptor;
	@Override
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor) {
		try {
			
			this.adaptor = adaptor;
			timer = new SingleThreadCPUTimer();
			timer.startTimer();
			setTimerStop(timer);
			
			createParetoFront();
			queue = new PriorityQueue<>();
			
			configureOrderForObjectives(adaptor);
			
			EfficientSolution lower = lexicoGraphicalMinimum(secondObjective(), firstObjective());
			reportEfficientSolution(lower);

			EfficientSolution upper = lexicoGraphicalMinimum(firstObjective(), secondObjective());			
			reportEfficientSolution(upper);
			
			PairOfEfficientSolutions pair = new PairOfEfficientSolutions(lower, upper);
			queue.add(pair);
			
			while (!timer.shouldStop() && !queue.isEmpty()) {
				pair = queue.poll();
				
				if (!tooSmall(pair)) {
					EfficientSolution intermediate = getNonDominatedSolutionInRectangle (pair);
					if (pair.isStrictlyIncludedInRegion(intermediate)) {
						reportEfficientSolution(intermediate);

						PairOfEfficientSolutions firstPair = pair.clone();
						firstPair.upper = intermediate;
						queue.add(firstPair);
						
						PairOfEfficientSolutions secondPair = pair.clone();
						secondPair.lower = intermediate;
						queue.add(secondPair);
						
					} else {
						addNewPairIfRequired(pair);
						reportInnecesaryRunOfILPSolver();
					}
				}
			}			
			return paretoFront;
		} catch (IloException e) {
			throw new RuntimeException (e);
		}
	}
	
	protected abstract EfficientSolution getNonDominatedSolutionInRectangle(PairOfEfficientSolutions pair) throws IloException;
	protected abstract void addNewPairIfRequired(PairOfEfficientSolutions pair);
	
	
	protected void reportInnecesaryRunOfILPSolver() {
		if (listener!=null) {
			listener.reportInnecesaryRunOfSolver();
		}
	}

	protected boolean tooSmall(PairOfEfficientSolutions pair) {
		int objectives = pair.lower.getNumberOfObjectives();
		for (int i=0; i < objectives; i++) {
			if (pair.spanInObjective(i) <= 1) {
				return true;
			}
		}
		return false;
	}
		

	protected double getEpsilonForPair(PairOfEfficientSolutions pair) {
		return (pair.upper.getObjectiveValue(secondObjective()) + pair.lower.getObjectiveValue(secondObjective()))/2;
	}

	protected void reportLambda(double lambda) {
		if (listener != null) {
			listener.info("lambda", ""+lambda);
		}
	}

	protected EfficientSolution lexicoGraphicalMinimum(int firstObjective, int secondObjective) throws IloException {
		
		Modelo modelo = adaptor.ilpModelForConstraints();
		modelo.cplex.addMinimize(adaptor.getObjective(firstObjective));
		solveIlpInstance(modelo);
		
		double firstObjValue = (int)Math.round(modelo.cplex.getObjValue());
		
		modelo = adaptor.ilpModelForConstraints();
		modelo.cplex.addLe(adaptor.getObjective(firstObjective), firstObjValue);
		modelo.cplex.addMinimize(adaptor.getObjective(secondObjective));

		solveIlpInstance(modelo);

		double secondObjValue = (int)Math.round(modelo.cplex.getObjValue());
		
		double[] values = new double [2];
		values[firstObjective] = firstObjValue;
		values[secondObjective] = secondObjValue;
		return new EfficientSolutionWithTimeStamp(values, timer.elapsedTimeInMilliseconds());
	}

	protected double evaluateLinearExpression(Modelo modelo, IloLinearNumExpr objective) throws IloException {
		double result = 0.0;
		for (IloLinearNumExprIterator iterator = objective.linearIterator(); iterator.hasNext();) {
			IloNumVar variable = iterator.nextNumVar();
			double coefficient = iterator.getValue();
			
			try {
				result += coefficient * modelo.cplex.getValue(variable);
			} catch (UnknownObjectException e) {
				result += Math.min(0, coefficient);
			}
		}
		return result;
	}

}
