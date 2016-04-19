package neo.requirements.sat.cplex;

public interface CplexAdaptor {

	public Modelo ilpInstanceForMinimizingEffort(int valueLowerBound);
	public Modelo ilpInstanceForMaximizingValue(int effortUpperBound);

}