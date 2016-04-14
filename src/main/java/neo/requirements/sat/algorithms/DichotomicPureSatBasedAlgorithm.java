package neo.requirements.sat.algorithms;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.minisatp.MinisatpSolver;
import neo.requirements.sat.minisatp.MinisatpSolver.Result;
import neo.requirements.sat.minisatp.NextReleaseToMinisatpAdaptor;
import NRPReaders.FileReader;
import NRPReaders.InMemoryReader;
import NRPReaders.NextReleaseProblemReader;

public class DichotomicPureSatBasedAlgorithm {
	
	private static class LabelledNonDominatedSolution implements Cloneable{
		public String solution;
		public int effort;
		public int value;
		public int index;
		
		public LabelledNonDominatedSolution(String solution, int effort, int value, int index) {
			this.solution = solution;
			this.effort=effort;
			this.value=value;
			this.index=index;
		}
		
		public LabelledNonDominatedSolution(LabelledNonDominatedSolution sol) {
			this.solution=sol.solution;
			this.effort = sol.effort;
			this.value=sol.value;
			this.index=sol.index;
		}
		
		public String toString() {
			return ""+effort+" "+value+" "+index;
		}

		
		
		
	}

	private MinisatpSolver minisatpSolver = new MinisatpSolver(false);
	private NextReleaseProblem problem;
	private NextReleaseToMinisatpAdaptor adaptor;
	private List<LabelledNonDominatedSolution> paretoFront;
	private int sizeParetoFront = 30;


	public DichotomicPureSatBasedAlgorithm(NextReleaseProblem problem) {
		this();
		setProblem(problem);
	}

	public DichotomicPureSatBasedAlgorithm() {}

	public void computeParetoFront () {
		Integer maxEffort = problem.sumOfAllRequirementsEffort();
		String minisatpInstance = adaptor.minisatpInstanceForMaximizingValue(maxEffort);
		Result requirementsValue = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MAXIMIZE);
		maxEffort = minimizeEffort(requirementsValue.value);
		
		paretoFront = new LinkedList<DichotomicPureSatBasedAlgorithm.LabelledNonDominatedSolution>();
		
		paretoFront.add(new LabelledNonDominatedSolution(null, 0, 0, 0));
		paretoFront.add(new LabelledNonDominatedSolution(null, maxEffort, requirementsValue.value, 0));
		
		int numerator=0;
		
		while (paretoFront.size() < sizeParetoFront) {
			numerator++;
			ListIterator<LabelledNonDominatedSolution> iterator = paretoFront.listIterator();
			while (iterator.hasNext()) {
				LabelledNonDominatedSolution labelledSolution = iterator.next();
				
				if (!iterator.hasNext()) {
					continue;
				}
				
				labelledSolution.index <<= 1;
				LabelledNonDominatedSolution newLabelledSolution = new LabelledNonDominatedSolution (labelledSolution);
				newLabelledSolution.index |= 1;
				long effort = (((long)newLabelledSolution.index)*maxEffort)>>>numerator;
				
				minisatpInstance = adaptor.minisatpInstanceForMaximizingValue((int)effort);
				requirementsValue = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MAXIMIZE);
				
				effort = minimizeEffort(requirementsValue.value);
				newLabelledSolution.solution="";
				newLabelledSolution.effort=(int)effort;
				newLabelledSolution.value=requirementsValue.value;
				
				System.out.println(newLabelledSolution);
				
				iterator.add(newLabelledSolution);
				
			}
			
		}
		
		
	}
	
	

	private Integer minimizeEffort(Integer value) {
		String minisatpInstance = adaptor.minisatpInstanceForMinimizingEffort(value);
		Result requirementsEffort = minisatpSolver.solveMinisatpInstance(minisatpInstance, MinisatpSolver.SearchDirection.MINIMIZE);
		if (requirementsEffort != null) {
			reportParetoFrontPoint(requirementsEffort, value);
			return requirementsEffort.value;
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
		DichotomicPureSatBasedAlgorithm algorithm = new DichotomicPureSatBasedAlgorithm(problem);
		algorithm.computeParetoFront();
	}

}
