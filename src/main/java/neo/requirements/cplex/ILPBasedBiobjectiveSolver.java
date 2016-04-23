package neo.requirements.cplex;

import java.util.List;
import java.util.Properties;

import neo.requirements.util.EfficientSolution;

public interface ILPBasedBiobjectiveSolver {
	public void setListener(ILPSolverListener listener);
	public List<EfficientSolution> computeParetoFront(ILPAdaptor adaptor);
	public String getName();
	public String getCommandLineName();
	public void setConfiguration(Properties configuration);

}
