package NRPReaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import neo.requirements.sat.NextReleaseProblem;
import neo.requirements.sat.NextReleaseProblem.Constraint;
import neo.requirements.sat.NextReleaseProblem.ConstraintType;

public class FileReader implements NextReleaseProblemReader {
	
	private File file;
	private int [] weightOfCustomers;
	private int [][] valueOfRequirements;
	private int [] effortOfREquirements;
	private List<Constraint> constraints;
	
	public FileReader(File file) {
		this.file = file;
	}

	public NextReleaseProblem readInstance() {
		try {
			Scanner scanner = new Scanner(file);
			int requirements = scanner.nextInt();
			int customers = scanner.nextInt();
			int limit = scanner.nextInt();
			
			intiializeDataStructures(requirements, customers);
			readWeightOfCustomers(scanner);
			readValueOfRequirements(scanner);
			readConstraints(scanner);
			
			return new NextReleaseProblem(valueOfRequirements, 
					effortOfREquirements, weightOfCustomers, constraints);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	private void intiializeDataStructures(int requirements, int customers) {
		weightOfCustomers = new int [customers];
		valueOfRequirements = new int[requirements][customers];
		effortOfREquirements = new int [requirements];
		constraints = new ArrayList<Constraint>();
	}
	
	private void readWeightOfCustomers(Scanner scanner) {
		for (int i=0; i < weightOfCustomers.length; i++) {
			weightOfCustomers[i] = scanner.nextInt();
		}
	}

	private void readValueOfRequirements(Scanner scanner) {
		for (int requirement = 0; requirement < valueOfRequirements.length; requirement++) {
			effortOfREquirements[requirement] = scanner.nextInt();
			for (int customer=0; customer < valueOfRequirements[requirement].length; customer++) {
				valueOfRequirements[requirement][customer] = scanner.nextInt();
			}
		}
	}
	
	private void readConstraints(Scanner scanner) {
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine().trim();
			if (!line.isEmpty()) {
				Constraint constraint = readConstraint(line);
				if (constraint!=null) {
					constraints.add(constraint);
				}
			}
		}
		
	}

	private Constraint readConstraint(String line) {
		String requirementNamePattern = "r[0-9]+";
		String operatorPattern = "·|\\+|->";
		Pattern constraintPattern = Pattern.compile("("+requirementNamePattern+")"+
				"("+operatorPattern+")"+"("+requirementNamePattern+")");
		
		Matcher matcher = constraintPattern.matcher(line);
		if (matcher.matches()) {
			String firstRequirement = matcher.group(1);
			String operator = matcher.group(2);
			String secondRequirement = matcher.group(3);

			return new Constraint(parseOperator(operator), 
					parseRequirement(firstRequirement), 
					parseRequirement(secondRequirement));
		}
		return null;
	}
	
	private ConstraintType parseOperator(String operator) {
		if ("·".equals(operator)) {
			return ConstraintType.SIMULTANEOUS;
		} else if ("+".equals(operator)) {
			return ConstraintType.EXCLUSION;
		} else if ("->".equals(operator)) {
			return ConstraintType.IMPLICATION;
		} else {
			return null;
		}	
	}
	
	private int parseRequirement(String requirementName) {
		return Integer.parseInt(requirementName.substring(1))-1;
	}

	

}
