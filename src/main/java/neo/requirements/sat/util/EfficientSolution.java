package neo.requirements.sat.util;

public class EfficientSolution {
	private double [] objectiveValues;
	
	public EfficientSolution(double [] values) {
		objectiveValues = values.clone();
	}
	
	public double getObjectiveValue(int i) {
		return objectiveValues[i];
	}

}
