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

public class ClassicInstancesReader implements NextReleaseProblemReader {
	
	private File file;
	private int [] weightOfCustomers;
	private int [][] valueOfRequirementForCustomer;
	private int [] costOfRequirements;
	private List<Constraint> constraints;
	
	public ClassicInstancesReader(File file) {
		this.file = file;
	}

	public NextReleaseProblem readInstance() {
		try {
			Scanner scanner = new Scanner(file);
			
			readCostOfRequirements(scanner);
			readConstraintsNew(scanner);
			readCustomers(scanner);
			
			return new NextReleaseProblem(valueOfRequirementForCustomer, 
					costOfRequirements, weightOfCustomers, constraints);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

	protected void readCustomers(Scanner scanner) {
		int customers = scanner.nextInt();
		weightOfCustomers = new int [customers];
		valueOfRequirementForCustomer = new int [costOfRequirements.length][customers];
		for (int customer = 0; customer < customers; customer++) {
			weightOfCustomers[customer] = scanner.nextInt();
			int requirements = scanner.nextInt();
			for (int requirement=0; requirement < requirements; requirement++) {
				valueOfRequirementForCustomer[scanner.nextInt()-1][customer] = 1;
			}
		}
	}

	protected void readConstraintsNew(Scanner scanner) {
		constraints = new ArrayList<Constraint>();
		int dependencies = scanner.nextInt();
		for (int dependency=0; dependency < dependencies; dependency++) {
			int secondRequirement = scanner.nextInt();
			int firstREquirement = scanner.nextInt();
			Constraint constraint = new Constraint(ConstraintType.IMPLICATION, firstREquirement, secondRequirement);
			constraints.add(constraint);
		}
	}

	protected void readCostOfRequirements(Scanner scanner) {
		int levels = scanner.nextInt();
		List<Integer> costs = new ArrayList<Integer>();
		for (int level=0; level< levels; level++) {
			int requirementsInLevel = scanner.nextInt();
			for (int requirement=0; requirement < requirementsInLevel; requirement++) {
				costs.add(scanner.nextInt());
			}
		}
		
		costOfRequirements = new int [costs.size()];
		for (int i = 0; i < costOfRequirements.length; i++) {
			costOfRequirements[i] = costs.get(i);
		}
		
	}
	
}
