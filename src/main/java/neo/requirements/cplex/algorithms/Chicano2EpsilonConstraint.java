package neo.requirements.cplex.algorithms;

public class Chicano2EpsilonConstraint extends ChicanoEpsilonConstraint {

	@Override
	protected int secondObjective() {
		return 0;
	}

	@Override
	protected int firstObjective() {
		return 1;
	}

	@Override
	public String getName() {
		return super.getName()+ " (swapping order)";
	}

	@Override
	public String getCommandLineName() {
		return super.getCommandLineName()+"2";
	}

}
