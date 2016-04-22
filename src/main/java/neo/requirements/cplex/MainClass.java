package neo.requirements.cplex;

import java.io.File;
import java.util.List;

import neo.requirements.cplex.algorithms.ChicanoEpsilonConstraint;
import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.readers.ClassicInstancesReader;
import neo.requirements.problems.readers.FileReader;
import neo.requirements.problems.readers.InMemoryReader;
import neo.requirements.problems.readers.NextReleaseProblemReader;
import neo.requirements.util.EfficientSolution;
import neo.requirements.util.SingleThreadCPUTimer;

public class MainClass {

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
		ILPBasedBiobjectiveSolver solver = new ChicanoEpsilonConstraint();
		solver.setListener(new ILPSolverListener() {
			@Override
			public void notifyEfficientSolutionFound(EfficientSolution solution) {
				System.out.println("Found: "+solution);
				
			}
		});
		ILPAdaptor adaptor = new NRPCplexILPAdaptor(problem);
		List<EfficientSolution> paretoFront = solver.computeParetoFront(adaptor);
		
		System.out.println("Pareto Front");
		System.out.println("------------");
		for (EfficientSolution solution: paretoFront) {
			System.out.println(solution);
		}
		System.out.println(paretoFront.size()+ " efficient solutions computed");
		System.out.println("Time: "+timer.elapsedTimeInMilliseconds()+ " ms");
	}


}
