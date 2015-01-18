package neo.requirements.sat.minisatp;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.NextReleaseProblem.Constraint;

public class NextReleaseToMinisatpAdaptor {
	private String effortExpression;
	private String positiveValueExpression;
	private String negativeValueExpression;
	private List<String> minisatpConstraints;
	private NextReleaseProblem nextReleaseProblem;

	public NextReleaseToMinisatpAdaptor(NextReleaseProblem nrp) {
		nextReleaseProblem=nrp;
	}
	
	public String minisatpInstanceForMinimizingEffort (int valueLowerBound) {
		String effortExpression = getEffortExpression();
		String positiveValueExpression = getPositiveValueExpression();
		
		StringWriter minisatpInstance = new StringWriter();
		PrintWriter writer = new PrintWriter(minisatpInstance);
	
		writer.println("min: "+effortExpression+";");
		writer.println(positiveValueExpression + " >= "+valueLowerBound+";");
		for (String constraint: minisatpConstraints()) {
			writer.println(constraint+";");
		}
		writer.close();
	
		return minisatpInstance.toString();
	}

	public String minisatpInstanceForMaximizingValue (int effortUpperBound) {
		
		String negativeValueExpression = getNegativeValueExpression();
		String effortExpression = getEffortExpression();
			
		StringWriter minisatpInstance = new StringWriter();
		PrintWriter writer = new PrintWriter(minisatpInstance);
	
		writer.println("min: "+negativeValueExpression+";");
		writer.println(effortExpression + " <= "+effortUpperBound+";");
		for (String constraint: minisatpConstraints()) {
			writer.println(constraint+";");
		}
		writer.close();
	
		return minisatpInstance.toString();
	}

	private String getEffortExpression() {
		if (effortExpression==null) {
			effortExpression=computeEffortExpression();
		}
		return effortExpression;
	}

	private String computeEffortExpression() {
		String string = "";
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			string += minisatpStringForInteger(nextReleaseProblem.getEffortOfRequirement(requirement)) +" "+
		              getRequirementMinisatpName(requirement);
			if (requirement < nextReleaseProblem.getRequirements()-1) {
				string += " ";
			}
		}
		return string;
	}
	
	private String getPositiveValueExpression() {
		if (positiveValueExpression==null) {
			positiveValueExpression=computeValueExpression(1);
		}
		return positiveValueExpression;
	}
	
	private String getNegativeValueExpression() {
		if (negativeValueExpression==null) {
			negativeValueExpression = computeValueExpression(-1);
		}
		return negativeValueExpression;
	}

	private String computeValueExpression(int multiplier) {
		String string = "";
		for (int requirement=0; requirement < nextReleaseProblem.getRequirements(); requirement++) {
			string += minisatpStringForInteger(multiplier*nextReleaseProblem.totalValueForRequirement(requirement)) +" "+	
		              getRequirementMinisatpName(requirement);
			if (requirement < nextReleaseProblem.getRequirements()-1) {
				string += " ";
			}
		}
		return string;
	}

	private List<String> minisatpConstraints() {
		if (minisatpConstraints == null) {
			minisatpConstraints=computeMinisatpConstraints();
		}
		return minisatpConstraints;
	}
	
	private List<String> computeMinisatpConstraints() {
		List<String> minisatpConstraints = new ArrayList<String>();
		for (Constraint constraint: nextReleaseProblem.getConstraints()) {
			minisatpConstraints.addAll(minisatpConstraintsForConstraint(constraint));
		}
		return minisatpConstraints;
	}
	
	private List<String> minisatpConstraintsForConstraint(Constraint constraint) {		
		switch (constraint.type) {
			case IMPLICATION:
				return implicationConstraintToMinisatp(constraint.firstRequirement, constraint.secondRequirement);
			case EXCLUSION:
				return exclusionConstraintToMinisatp(constraint.firstRequirement, constraint.secondRequirement);
			case SIMULTANEOUS:
				return simultaneousConstraintToMinisatp(constraint.firstRequirement, constraint.secondRequirement);
			default:
				return null;
		}		
	}

	private List<String> exclusionConstraintToMinisatp(int firstRequirement, int secondRequirement) {
		String minisatpConstraint = "-1 "+getRequirementMinisatpName(firstRequirement)+
				" -1 "+getRequirementMinisatpName(secondRequirement)+
				" >= -1";
		return Arrays.asList(minisatpConstraint);
	}

	private List<String> implicationConstraintToMinisatp(int firstRequirement, int secondRequirement) {
		String minisatpConstraint = "-1 "+getRequirementMinisatpName(firstRequirement)+
				" +1 "+getRequirementMinisatpName(secondRequirement)+
				" >= 0";
		return Arrays.asList(minisatpConstraint);
	}
	
	private List<String> simultaneousConstraintToMinisatp(int firstRequirement, int secondRequirement) {
		List<String> minisatpConstraints = new ArrayList<String>();
		minisatpConstraints.addAll(implicationConstraintToMinisatp(firstRequirement, secondRequirement));
		minisatpConstraints.addAll(implicationConstraintToMinisatp(secondRequirement, firstRequirement));
		return minisatpConstraints;
	}

	private String getRequirementMinisatpName(int requirement) {
		return "r"+(requirement+1);
	}

	private String minisatpStringForInteger(int value) {
		return ((value > 0)?"+":"")+value;
	}

	
}