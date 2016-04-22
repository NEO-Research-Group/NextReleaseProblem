package neo.requirements.cplex.old;

import neo.requirements.cplex.Modelo;

public interface CplexAdaptor {

	public Modelo ilpInstanceForMinimizingEffort(int valueLowerBound);
	public Modelo ilpInstanceForMaximizingValue(int effortUpperBound);

}