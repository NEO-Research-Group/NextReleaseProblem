package neo.requirements.cplex;

import java.io.File;

import neo.requirements.cplex.CplexSolver.Result;
import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.readers.ClassicInstancesReader;
import neo.requirements.problems.readers.FileReader;
import neo.requirements.problems.readers.InMemoryReader;
import neo.requirements.problems.readers.NextReleaseProblemReader;
import neo.requirements.util.SingleThreadCPUTimer;

public class CplexBasedAlgorithm {

	private CplexSolver cplexSolver = new CplexSolver();
	private NextReleaseProblem problem;
	private CplexAdaptor adaptor;


	public CplexBasedAlgorithm(NextReleaseProblem problem) {
		this();
		setProblem(problem);
	}

	public CplexBasedAlgorithm() {}

	public void computeParetoFront () {
		Integer effort = problem.sumOfAllRequirementsEffort();
		boolean compute=true;
		while (compute) {
			Modelo ilpInstance = adaptor.ilpInstanceForMaximizingValue(effort);
			Result requirementsValue = cplexSolver.solveIlpInstance(ilpInstance);
			if (requirementsValue != null) {
				effort = minimizeEffort(requirementsValue.value);
			}
			compute = (requirementsValue!=null && effort!=null);
		}
	}

	private Integer minimizeEffort(Integer value) {
		Modelo ilpInstance = adaptor.ilpInstanceForMinimizingEffort(value);
		Result requirementsEffort = cplexSolver.solveIlpInstance(ilpInstance);
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
		switch (problem.getKindOfInstance()) {
		case ALMERIA:
			adaptor = new NextReleaseToCplexAdaptor(problem);
			break;
		case XUAN:
			adaptor = new XuanNRPToCplexAdaptor(problem);
			break;
			default:
				throw new RuntimeException("Unsupported kind of instance");
		}
	}

	public static void main (String [] args) {
		NextReleaseProblemReader reader;
		SingleThreadCPUTimer timer = new SingleThreadCPUTimer();
		timer.startTimer();
		if (args.length > 0) {
			if (args[0].equals("-xuan")) {
				File instancia = new File(args[1]);
				reader = new ClassicInstancesReader(instancia);
			} else {
				File instancia = new File(args[0]);
				reader = new FileReader(instancia);
			}
			
		} else {
			reader = new InMemoryReader();
		}
		
		NextReleaseProblem problem = reader.readInstance();
		CplexBasedAlgorithm algorithm = new CplexBasedAlgorithm(problem);
		algorithm.computeParetoFront();
		System.out.println("Time: "+timer.elapsedTimeInMilliseconds()+ " ms");
	}

}
