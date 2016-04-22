package neo.requirements.sat.util;

public class EfficientSolutionWithTimeStamp extends EfficientSolution {
	private long timestamp;
	
	public EfficientSolutionWithTimeStamp(double [] values, long timestamp) {
		super(values);
		this.timestamp = timestamp;
	}
	
	public long getTimestamp() {
		return timestamp;
	}
	
	public String toString() {
		return super.toString() + " time: "+timestamp;
	}

}
