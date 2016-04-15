package neo.requirements.sat.algorithms;

import java.io.File;

import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.cplex.CplexSolver;
import neo.requirements.sat.cplex.CplexSolver.Result;
import neo.requirements.sat.cplex.Modelo;
import neo.requirements.sat.cplex.NextReleaseToCplexAdaptor;
import NRPReaders.FileReader;
import NRPReaders.InMemoryReader;
import NRPReaders.NextReleaseProblemReader;

public class CplexBasedAlgorithm {

	private CplexSolver cplexSolver = new CplexSolver();
	private NextReleaseProblem problem;
	private NextReleaseToCplexAdaptor adaptor;


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
		adaptor = new NextReleaseToCplexAdaptor(problem);
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
		CplexBasedAlgorithm algorithm = new CplexBasedAlgorithm(problem);
		algorithm.computeParetoFront();
	}

}
