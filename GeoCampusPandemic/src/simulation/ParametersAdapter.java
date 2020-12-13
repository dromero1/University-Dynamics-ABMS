package simulation;

import model.learning.LearningStyle;
import model.learning.SelectionStrategy;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;

public final class ParametersAdapter {

	/**
	 * Exposed students parameter id
	 */
	private static final String EXPOSED_STUDENTS_PARAM_ID = "exposedStudents";

	/**
	 * Susceptible students parameter id
	 */
	private static final String SUSCEPTIBLE_STUDENTS_COUNT_PARAM_ID = "susceptibleStudents";

	/**
	 * Susceptible staffers parameter id
	 */
	private static final String SUSCEPTIBLE_STAFFERS_COUNT_PARAM_ID = "susceptibleStaffers";

	/**
	 * Infection radius parameter id
	 */
	private static final String INFECTION_RADIUS_PARAM_ID = "infectionRadius";

	/**
	 * Particle expulsion interval parameter id
	 */
	private static final String PARTICLE_EXPULSION_INTERVAL_PARAM_ID = "particleExpulsionInterval";

	/**
	 * Vehicle usage ratio parameter id
	 */
	private static final String VEHICLE_USAGE_RATIO_PARAM_ID = "vehicleUsageRatio";

	/**
	 * Social distancing parameter id
	 */
	private static final String SOCIAL_DISTANCING_PARAM_ID = "socialDistancing";

	/**
	 * Selection strategy parameter id
	 */
	private static final String SELECTION_STRATEGY_PARAM_ID = "selectionStrategy";

	/**
	 * Learning style parameter id
	 */
	private static final String LEARNING_STYLE_PARAM_ID = "learningStyle";

	/**
	 * Epsilon parameter id
	 */
	private static final String EPSILON_PARAM_ID = "epsilon";

	/**
	 * Learning rate parameter id
	 */
	private static final String LEARNING_RATE_PARAM_ID = "learningRate";

	/**
	 * Discount factor parameter id
	 */
	private static final String DISCOUNT_FACTOR_PARAM_ID = "discountFactor";

	/**
	 * Outbreak tick parameter id
	 */
	private static final String OUTBREAK_TICK_PARAM_ID = "outbreakTick";

	/**
	 * Minimum initial Q-value parameter id
	 */
	private static final String MIN_INIT_Q_VALUE_PARAM_ID = "minInitQValue";

	/**
	 * Maximum initial Q-value parameter id
	 */
	private static final String MAX_INIT_Q_VALUE_PARAM_ID = "maxInitQValue";

	/**
	 * Private constructor
	 */
	private ParametersAdapter() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Get exposed students
	 */
	public static int getExposedStudents() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getInteger(EXPOSED_STUDENTS_PARAM_ID);
	}

	/**
	 * Get susceptible students
	 */
	public static int getSusceptibleStudents() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getInteger(SUSCEPTIBLE_STUDENTS_COUNT_PARAM_ID);
	}

	/**
	 * Get susceptible staffers
	 */
	public static int getSusceptibleStaffers() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getInteger(SUSCEPTIBLE_STAFFERS_COUNT_PARAM_ID);
	}

	/**
	 * Get infection radius
	 */
	public static double getInfectionRadius() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(INFECTION_RADIUS_PARAM_ID);
	}

	/**
	 * Get particle expulsion interval
	 */
	public static double getParticleExpulsionInterval() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(PARTICLE_EXPULSION_INTERVAL_PARAM_ID);
	}

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
		} else if (value.equals("Bandits")) {
			return LearningStyle.BANDITS;
		}
		return null;
	}

	/**
	 * Get epsilon
	 */
	public static double getEpsilon() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(EPSILON_PARAM_ID);
	}

	/**
	 * Get learning rate
	 */
	public static double getLearningRate() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(LEARNING_RATE_PARAM_ID);
	}

	/**
	 * Get discount factor
	 */
	public static double getDiscountFactor() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(DISCOUNT_FACTOR_PARAM_ID);
	}

	/**
	 * Get outbreak tick
	 */
	public static double getOutbreakTick() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(OUTBREAK_TICK_PARAM_ID);
	}

	/**
	 * Get minimum initial Q-value
	 */
	public static double getMinimumInitialQValue() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(MIN_INIT_Q_VALUE_PARAM_ID);
	}

	/**
	 * Get maximum initial Q-value
	 */
	public static double getMaximumInitialQValue() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		return simParams.getDouble(MAX_INIT_Q_VALUE_PARAM_ID);
	}

}