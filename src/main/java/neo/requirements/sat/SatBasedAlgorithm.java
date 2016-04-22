package neo.requirements.sat;

import java.io.File;

import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.readers.FileReader;
import neo.requirements.problems.readers.InMemoryReader;
import neo.requirements.problems.readers.NextReleaseProblemReader;
import neo.requirements.sat.minisatp.MinisatpSolver;
import neo.requirements.sat.minisatp.NextReleaseToMinisatpAdaptor;
import neo.requirements.sat.minisatp.MinisatpSolver.Result;

public class SatBasedAlgorithm {

	MinisatpSolver minisatpSolver = new MinisatpSolver(true);
	private NextReleaseProblem problem;
	private NextReleaseToMinisatpAdaptor adaptor;


	public SatBasedAlgorithm(NextReleaseProblem problem) {
		this();
		setProblem(problem);
	}

	public SatBasedAlgorithm() {}

	public void computeParetoFront () {
		Integer effort = problem.sumOfAllRequirementsEffort();
		boolean compute=true;
		while (compute) {
			String minisatpInstance = adaptor.minisatpInstanceForMaximizingValue(effort);
			Result requirementsValue = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MAXIMIZE);
			if (requirementsValue != null) {
				effort = minimizeEffort(requirementsValue.value);
			}
			compute = (requirementsValue!=null && effort!=null);
		}
	}

	private Integer minimizeEffort(Integer value) {
		String minisatpInstance = adaptor.minisatpInstanceForMinimizingEffort(value);
		Result requirementsEffort = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MINIMIZE);
		if (requirementsEffort != null) {
			reportParetoFrontPoint(requirementsEffort, value);
			return requirementsEffort.value-1;
		} else {
			return null;
		}
	}

	private void reportParetoFrontPoint (Result requirementsEffort, int value) {
		System.out.println("Effort: "+requirementsEffort.value+", Value: "+value+", Solution: "+requirementsEffort.solution);
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
		SatBasedAlgorithm algorithm = new SatBasedAlgorithm(problem);
		algorithm.computeParetoFront();
	}

}
