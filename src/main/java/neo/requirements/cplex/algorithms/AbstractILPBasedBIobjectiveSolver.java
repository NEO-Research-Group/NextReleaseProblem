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
import neo.requirements.util.EfficientSolutionWithTimeStamp;
import neo.requirements.util.SingleThreadCPUTimer;

public abstract class AbstractILPBasedBIobjectiveSolver  implements ILPBasedBiobjectiveSolver{
	
	public static final double DEFAULT_EPGAP = 1E-9;
	public static final double DEFAULT_EPAGAP = 1E-9;
	public static final double DEFAULT_EPOPT = 1E-9;
	public static final double DEFAULT_EPINT = 1E-9;
	
	
	

	protected ILPSolverListener listener;
	protected List<EfficientSolution> paretoFront;
	protected Properties configuration;
	protected int step = 1;
	protected boolean naturalOrderForObjectives = true;
	
	private double epInt = DEFAULT_EPINT;
	private double epOpt = DEFAULT_EPOPT;
	private double epAGap = DEFAULT_EPAGAP;
	private double epGap = DEFAULT_EPGAP;
	

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
			modelo.cplex.setParam(IloCplex.DoubleParam.EpInt, epInt);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpGap, epGap);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpAGap, epAGap);
			modelo.cplex.setParam(IloCplex.DoubleParam.EpOpt, epOpt);
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
		configureEpsilonConstants();
	}

	private void configureEpsilonConstants() {
		String property = configuration.getProperty("EpInt");
		if (property!=null) {
			epInt = Double.parseDouble(property);
		}
		
		property = configuration.getProperty("EpOpt");
		if (property!=null) {
			epOpt = Double.parseDouble(property);
		}
		
		property = configuration.getProperty("EpGap");
		if (property!=null) {
			epGap = Double.parseDouble(property);
		}
		
		property = configuration.getProperty("EpAGap");
		if (property!=null) {
			epAGap = Double.parseDouble(property);
		}
	}

	protected void configureStep() {
		String propertyValue = configuration.getProperty("step");
		if (propertyValue != null) {
			step = Integer.parseInt(propertyValue);
		}
	}

	protected void configureOrderForObjectives(ILPAdaptor adaptor) {
		if (configuration != null) {
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

	protected EfficientSolutionWithTimeStamp buildEfficientSolution(SingleThreadCPUTimer timer, double firstObjValue,
			double secondObjValue) {
				
				double[] values = new double [2];
				values[firstObjective()] = firstObjValue;
				values[secondObjective()] = secondObjValue;
				return new EfficientSolutionWithTimeStamp(values,  timer.elapsedTimeInMilliseconds());
			}

	protected void setTimerStop(SingleThreadCPUTimer timer) {
		String property = configuration.getProperty("time");
		if (property != null) {
			timer.setStopTimeMilliseconds(Integer.parseInt(property)*1000);
		}
	}

}