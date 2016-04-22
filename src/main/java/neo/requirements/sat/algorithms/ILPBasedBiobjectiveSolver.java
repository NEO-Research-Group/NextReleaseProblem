package neo.requirements.sat.algorithms;

import java.util.List;

import neo.requirements.sat.cplex.ILPAdaptor;
import neo.requirements.sat.util.EfficientSolutionWithTimeStamp;

public interface ILPBasedBiobjectiveSolver {
	public List<EfficientSolutionWithTimeStamp> computeParetoFront(ILPAdaptor adaptor);

}
