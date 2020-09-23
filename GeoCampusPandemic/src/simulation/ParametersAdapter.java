package simulation;

import model.learning.LearningStyle;
import model.learning.SelectionStrategy;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public class ParametersAdapter {

	/**
	 * Vehicle usage ratio parameter id
	 */
	private static final String VEHICLE_USAGE_RATIO_PARAM_ID = "vehicleUsageRatio";

	/**
	 * Social distancing parameter id
	 */
	private static final String SOCIAL_DISTANCING_PARAM_ID = "socialDistancing";

	/**
	 * Infection radius parameter id
	 */
	private static final String INFECTION_RADIUS_PARAM_ID = "infectionRadius";

	/**
	 * Selection strategy parameter id
	 */
	private static final String SELECTION_STRATEGY_PARAM_ID = "selectionStrategy";

	/**
	 * Learning style parameter id
	 */
	private static final String LEARNING_STYLE_PARAM_ID = "learningStyle";

	/**
	 * Get vehicle usage ratio
	 */
	public static double getVehicleUsageRatio() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(VEHICLE_USAGE_RATIO_PARAM_ID);
	}

	/**
	 * Get social distancing
	 */
	public static double getSocialDistancing() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(SOCIAL_DISTANCING_PARAM_ID);
	}

	/**
	 * Get infection radius
	 */
	public static double getInfectionRadius() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(INFECTION_RADIUS_PARAM_ID);
	}

	/**
	 * Get selection strategy
	 */
	public static SelectionStrategy getSelectionStrategy() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		String value = simParams.getString(SELECTION_STRATEGY_PARAM_ID);
		if (value.equals("random")) {
			return SelectionStrategy.RANDOM;
		} else if (value.equals("RL-based")) {
			return SelectionStrategy.RL_BASED;
		}
		return null;
	}

	/**
	 * Get learning style
	 */
	public static LearningStyle getLearningStyle() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		String value = simParams.getString(LEARNING_STYLE_PARAM_ID);
		if (value.equals("Q-learning")) {
			return LearningStyle.Q_LEARNING;
		} else if (value.equals("SARSA")) {
			return LearningStyle.SARSA;
		} else if (value.equals("Bandits")) {
			return LearningStyle.BANDITS;
		}
		return null;
	}

	/**
	 * Private constructor
	 */
	private ParametersAdapter() {
		throw new UnsupportedOperationException("Utility class");
	}

}