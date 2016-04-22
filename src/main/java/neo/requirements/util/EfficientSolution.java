package neo.requirements.util;

import java.util.Arrays;

public class EfficientSolution {
	private double [] objectiveValues;
	
	public EfficientSolution(double [] values) {
		objectiveValues = values.clone();
	}
	
	public double getObjectiveValue(int i) {
		return objectiveValues[i];
	}
	
	public int getNumberOfObjectives() {
		return objectiveValues.length;
	}
	
	public String toString() {
		return Arrays.toString(objectiveValues);
	}

}
