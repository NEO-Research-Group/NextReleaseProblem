package neo.requirements.sat;

import java.io.File;

import NRPReaders.FileReader;
import NRPReaders.InMemoryReader;
import NRPReaders.NextReleaseProblemReader;
import neo.requirements.sat.minisatp.MinisatpSolver;
import neo.requirements.sat.minisatp.NextReleaseToMinisatpAdaptor;

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
		Integer value = 0;

		while (effort != null && value != null) {
			String minisatpInstance = adaptor.minisatpInstanceForMaximizingValue(effort);
			value = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MAXIMIZE);
			if (value != null) {
				effort = minimizeEffort(value);
			}
		}
	}

	private Integer minimizeEffort(Integer value) {
		String minisatpInstance = adaptor.minisatpInstanceForMinimizingEffort(value);
		Integer effort = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MINIMIZE);
		if (effort != null) {
			reportParetoFrontPoint(effort, value);
			effort = effort-1;
		}
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
		SatBasedAlgorithm algorithm = new SatBasedAlgorithm(problem);
		algorithm.computeParetoFront();
	}

}
