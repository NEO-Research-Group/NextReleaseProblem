package neo.requirements.sat;

import java.util.List;

public class NextReleaseProblem {
	
	public static enum ConstraintType {IMPLICATION, EXCLUSION, SIMULTANEOUS}
	public static class Constraint {
		public ConstraintType type;
		public int firstRequirement;
		public int secondRequirement;
		
		public Constraint(ConstraintType type, int firstRequirement, int secondRequirement) {
			this.type = type;
			this.firstRequirement = firstRequirement;
			this.secondRequirement = secondRequirement;
		}
		
		public Constraint(){}
	}
	
	private int stakeholders;
	private int requirements;
	private int [][] valueOfRequirementsForStakeholders;
	private int [] effortOfRequirements;
	private int [] weightOfStakeholders;
	private List<Constraint> constraints;

	public NextReleaseProblem (int [][] valueOfRequirementsForStakeholders, 
			int [] effortOfREquirements,
			int [] weightOfStakeholder,
			List<Constraint> constraints){
		this.valueOfRequirementsForStakeholders = valueOfRequirementsForStakeholders;
		this.effortOfRequirements = effortOfREquirements;
		this.weightOfStakeholders = weightOfStakeholder;
		checkDimensions();
		
		this.constraints = constraints;
		
		stakeholders = weightOfStakeholder.length;
		requirements = effortOfREquirements.length;
	}
	
	
	private void checkDimensions() {
		if (effortOfRequirements.length != valueOfRequirementsForStakeholders.length) {
			throw new IllegalArgumentException("The dimensions are not correct");
		} else {
			for (int i=0; i < valueOfRequirementsForStakeholders.length; i++) {
				if (valueOfRequirementsForStakeholders[i].length != weightOfStakeholders.length) {
					throw new IllegalArgumentException("The dimensions are not correct");
				}
			}
		}
	}
	
	public int getStakeholders() {
		return stakeholders;
	}
	public int getRequirements() {
		return requirements;
	}
	
	public List<Constraint> getConstraints() {
		return constraints;
	}
	
	public int getValueOfRequirementForStakeholder (int requirement, int stakeholder) {
		return valueOfRequirementsForStakeholders[requirement][stakeholder];
	}
	
	public int getEffortOfRequirement(int requirement) {
		return effortOfRequirements[requirement];
	}
	
	public int getWeightOfStakeholder(int stakeholder) {
		return weightOfStakeholders[stakeholder];
	}
	
	public int totalValueForRequirement(int requirement) {
		int total = 0;
		int [] valuesOfRequirement = valueOfRequirementsForStakeholders[requirement];
		for (int stakeholer = 0; stakeholer < valuesOfRequirement.length; stakeholer++) {
			total += valuesOfRequirement[stakeholer] * weightOfStakeholders[stakeholer];
		}
		return total;
	}
	
	public int sumOfAllRequirementsEffort() {
		int sum = 0;
		for (int effort : effortOfRequirements) {
			sum+=effort;
		}
		return sum;
	}
	


}
