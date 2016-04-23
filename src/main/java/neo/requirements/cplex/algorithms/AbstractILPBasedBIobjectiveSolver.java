package neo.requirements.cplex.algorithms;

import ilog.concert.IloException;
import ilog.cplex.IloCplex;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import neo.requirements.cplex.ILPBasedBiobjectiveSolver;
import neo.requirements.cplex.ILPSolverListener;
import neo.requirements.cplex.Modelo;
import neo.requirements.util.EfficientSolution;

public abstract class AbstractILPBasedBIobjectiveSolver  implements ILPBasedBiobjectiveSolver{

	protected ILPSolverListener listener;
	protected List<EfficientSolution> paretoFront;
	protected Properties configuration;
	protected int step;
	protected boolean naturalOrderForObjectives;

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
		configureOrderForObjectives();
	}

	protected void configureStep() {
		step = 1;
		String propertyValue = configuration.getProperty("step");
		if (propertyValue != null) {
			step = Integer.parseInt(propertyValue);
		}
	}

	protected void configureOrderForObjectives() {
		naturalOrderForObjectives = true;
		String propertyValue = configuration.getProperty("objorder");
		switch (propertyValue) {
		case "natural":
			naturalOrderForObjectives = true;
			break;
		case "inverse":
			naturalOrderForObjectives = false;
			break;
		default:
			throw new IllegalArgumentException("Unrecognized order for objectives: "+propertyValue);
		}
	}

	protected int secondObjective() {
		return naturalOrderForObjectives?1:0;
	}

	protected int firstObjective() {
		return naturalOrderForObjectives?0:1;
	}

}