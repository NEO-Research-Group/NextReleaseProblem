package neo.requirements.sat;

import java.io.File;

import neo.requirements.sat.minisatp.MinisatpSolver;
import neo.requirements.sat.minisatp.MinisatpSolver.Result;
import neo.requirements.sat.minisatp.NextReleaseToMinisatpAdaptor;
import NRPReaders.FileReader;
import NRPReaders.InMemoryReader;
import NRPReaders.NextReleaseProblemReader;

public class PureSatBasedAlgorithm {

	MinisatpSolver minisatpSolver = new MinisatpSolver(false);
	private NextReleaseProblem problem;
	private NextReleaseToMinisatpAdaptor adaptor;


	public PureSatBasedAlgorithm(NextReleaseProblem problem) {
		this();
		setProblem(problem);
	}

	public PureSatBasedAlgorithm() {}

	public void computeParetoFront () {
		Integer effort = problem.sumOfAllRequirementsEffort();
		boolean compute=true;
		Result requirementsValue=null;
		while (compute) {
			if (requirementsValue == null) {
				String minisatpInstance = adaptor.minisatpInstanceForMaximizingValue(effort);
				requirementsValue = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MAXIMIZE);
			} else {
				requirementsValue = maximizeValue(effort, requirementsValue.value);
			}
			
			if (requirementsValue != null) {
				effort = minimizeEffort(effort, requirementsValue.value);
			}
			compute = (requirementsValue!=null && effort!=null && effort >= 0);
		}
	}
	
	

	private Result maximizeValue(Integer effort, int value) {
		String minisatInstance;
		do {
			value--;
			minisatInstance=adaptor.minisatDecisionInstance(effort, value);
		} while (!minisatpSolver.solveMinisatDecisionInstance(minisatInstance));
		
		Result result = new Result(value, null);
		return result;
	}

	private Integer minimizeEffort(Integer effort, Integer value) {
		String minisatInstance;
		do {
			effort--;
			minisatInstance=adaptor.minisatDecisionInstance(effort, value);
		} while (minisatpSolver.solveMinisatDecisionInstance(minisatInstance));		
		reportParetoFrontPoint(effort+1, value);
		
		return effort;
	}

	private void reportParetoFrontPoint (int effort, int value) {
		System.out.println("Effort: "+effort+", Value: "+value);
	}

	public void setProblem(NextReleaseProblem problem){
		this.problem = problem;
		adaptor = new NextReleaseToMinisatpAdaptor(problem);
	}

	public static void main (String [] args) {
		NextReleaseProblemReader reader;
		if (args.length > 0) {
			File instancia = new File(args[0]);
			reader = new FileReader(instancia);
		} else {
			reader = new InMemoryReader();
		}
		
		NextReleaseProblem problem = reader.readInstance();
		PureSatBasedAlgorithm algorithm = new PureSatBasedAlgorithm(problem);
		algorithm.computeParetoFront();
	}

}
