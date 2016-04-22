package neo.requirements.sat.algorithms;

import java.util.List;

import neo.requirements.sat.cplex.ILPAdaptor;
import neo.requirements.sat.util.EfficientSolution;

public interface ILPBasedBiobjectiveSolver {
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor);

}
