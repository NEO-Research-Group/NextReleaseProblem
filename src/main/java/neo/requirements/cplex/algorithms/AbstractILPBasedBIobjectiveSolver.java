package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import neo.requirements.cplex.ILPAdaptor;
import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.ILPSolverListener;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;

public abstract class AbstractILPBasedBIobjectiveSolver  implements ILPBasedBiobjectiveSolver{

	protected ILPSolverListener listener;
	protected List<EfficientSolution> paretoFront;
	protected Properties configuration;
	protected int step = 1;
	protected boolean naturalOrderForObjectives = true;

	public AbstractILPBasedBIobjectiveSolver() {
		super();
	}
	
	protected void reportEfficientSolution(EfficientSolution previousEfficientSolution) {
		notifyEfficientSolution(previousEfficientSolution);
		paretoFront.add(previousEfficientSolution);
	}

	private void notifyEfficientSolution(EfficientSolution efficientSolution) {
		if (listener != null) {
			listener.notifyEfficientSolutionFound(efficientSolution);
		}
		
	}

	protected boolean solveIlpInstance(Modelo modelo) throws IloException {	
			modelo.cplex.setOut(null);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpInt, 1E-9);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpGap, 1E-9);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpOpt, 1E-9);
			return modelo.cplex.solve();
	}

	@Override
	public void setListener(ILPSolverListener listener) {
		this.listener = listener;
	}

	protected void createParetoFront() {
		paretoFront = new ArrayList<>();
	}

	@Override
	public void setConfiguration(Properties configuration) {
		this.configuration = (Properties)configuration.clone();
		processConfigurationOptions();
	}

	private void processConfigurationOptions() {
		configureStep();
		//configureOrderForObjectives();
	}

	protected void configureStep() {
		String propertyValue = configuration.getProperty("step");
		if (propertyValue != null) {
			step = Integer.parseInt(propertyValue);
		}
	}

	protected void configureOrderForObjectives(ILPAdaptor adaptor) {
		String propertyValue = configuration.getProperty("objorder");
		if (propertyValue != null) {
			switch (propertyValue) {
			case "natural":
				naturalOrderForObjectives = true;
				break;
			case "inverse":
				naturalOrderForObjectives = false;
				break;
			case "adaptive":
				naturalOrderForObjectives = (computeObjectiveSpan(adaptor, 0) > computeObjectiveSpan(adaptor, 1));
				break;
			default:
				throw new IllegalArgumentException("Unrecognized order for objectives: "+propertyValue);
			}
		}
	}

	protected double computeObjectiveSpan(ILPAdaptor adaptor, int objective) {
		return adaptor.nadirUpperBound(objective)-adaptor.idealLowerBound(objective);
	}

	protected int secondObjective() {
		return naturalOrderForObjectives?1:0;
	}

	protected int firstObjective() {
		return naturalOrderForObjectives?0:1;
	}

}