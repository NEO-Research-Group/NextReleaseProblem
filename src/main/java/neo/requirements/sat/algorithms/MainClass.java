package neo.requirements.sat.algorithms;

import java.io.File;
import java.util.List;

import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.cplex.ILPAdaptor;
import neo.requirements.sat.cplex.NRPCplexILPAdaptor;
import neo.requirements.sat.util.EfficientSolution;
import neo.requirements.sat.util.SingleThreadCPUTimer;
import NRPReaders.ClassicInstancesReader;
import NRPReaders.FileReader;
import NRPReaders.InMemoryReader;
import NRPReaders.NextReleaseProblemReader;

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
		ILPAdaptor adaptor = new NRPCplexILPAdaptor(problem);
		List<EfficientSolution> paretoFront = solver.computeParetoFront(adaptor);
		
		for (EfficientSolution solution: paretoFront) {
			System.out.println(solution);
		}
		System.out.println(paretoFront.size()+ " efficient solutions computed");
		System.out.println("Time: "+timer.elapsedTimeInMilliseconds()+ " ms");
	}


}
