package neo.requirements.problems.readers;

import java.util.Arrays;
import java.util.List;

import neo.requirements.problems.NextReleaseProblem;
import neo.requirements.problems.NextReleaseProblem.Constraint;
import neo.requirements.problems.NextReleaseProblem.ConstraintType;

public class InMemoryReader implements NextReleaseProblemReader {

	private static int [][] valueOfRequirementsForStakeholders = {
			{4, 4, 5, 4, 5},
			{2, 4, 3, 5, 4}, 
			{1, 2, 3, 2, 2},
			{2, 2, 3, 3, 4},
			{5, 4, 4, 3, 5},
			
			{5, 5, 5, 4, 4},
			{2, 1, 2, 2, 2},
			{4, 4, 4, 4, 4},
			{4, 4, 4, 2, 5},
			{4, 5, 4, 3, 2},
			
			{2, 2, 2, 5, 4},
			{3, 3, 4, 2, 5},
			{4, 2, 1, 3, 3},
			{2, 4, 5, 2, 4},
			{4, 4, 4, 4, 4},
			
			{4, 2, 1, 3, 1},
			{4, 3, 2, 5, 1},
			{1, 2, 3, 4, 2},
			{3, 3, 3, 3, 4},
			{2, 1, 2, 2, 1}
	} ;
	private static int [] effortOfRequirements = {
		1, 4, 2, 3, 4, 7, 10, 2, 1, 3, 2, 5, 8, 2, 1, 4, 10, 4, 8, 4
	};
	private static int [] weightOfStakeholders = {
		1, 4, 2, 3, 4
	};
	private static List<Constraint> constraints = Arrays.asList(new Constraint[]{
		new Constraint(ConstraintType.SIMULTANEOUS, 2, 11),
		new Constraint(ConstraintType.SIMULTANEOUS, 10, 12),
		new Constraint(ConstraintType.IMPLICATION, 7, 3),
		new Constraint(ConstraintType.IMPLICATION, 16, 3),
		new Constraint(ConstraintType.IMPLICATION, 16, 7),
		new Constraint(ConstraintType.IMPLICATION, 2, 8),
		new Constraint(ConstraintType.IMPLICATION, 5, 8),
		new Constraint(ConstraintType.IMPLICATION, 11, 8),
		new Constraint(ConstraintType.IMPLICATION, 18, 8),
		new Constraint(ConstraintType.IMPLICATION, 18, 10)
	});
	
	private NextReleaseProblem nrp;
	
	public InMemoryReader() {
		
	}
	
	public NextReleaseProblem readInstance() {
		if (nrp != null) {
			return nrp;
		} else {
			nrp = prepareInstance();
		}
		return nrp;
	}

	private NextReleaseProblem prepareInstance() {
		return new NextReleaseProblem(valueOfRequirementsForStakeholders, effortOfRequirements, weightOfStakeholders, constraints);
	}

}
