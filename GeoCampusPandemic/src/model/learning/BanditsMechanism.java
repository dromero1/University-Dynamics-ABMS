package model.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gis.GISPolygon;
import repast.simphony.random.RandomHelper;

public class BanditsMechanism extends LearningMechanism {

	/**
	 * Q-value initialization from value
	 */
	private static final double Q_VALUE_INIT_TO = 0.0;

	/**
	 * Q-value initialization to value
	 */
	private static final double Q_VALUE_INIT_FROM = -1.0;

	/**
	 * Q-values for states
	 */
	protected Map<String, Double> qValues;

	/**
	 * Epsilon parameter for epsilon-greedy action selection
	 */
	protected static final double EPSILON = 0.1;

	/**
	 * Learning rate for update rule
	 */
	protected static final double LEARNING_RATE = 0.1;

	/**
	 * Create a new Bandits mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public BanditsMechanism(Map<String, GISPolygon> teachingFacilities, Map<String, GISPolygon> sharedAreas,
			Map<String, GISPolygon> eatingPlaces) {
		super(teachingFacilities, sharedAreas, eatingPlaces);
	}

	@Override
	public void init() {
		this.qValues = new HashMap<>();
		for (String eatingPlace : this.eatingPlaces.keySet()) {
			double q = RandomHelper.nextDoubleFromTo(Q_VALUE_INIT_FROM, Q_VALUE_INIT_TO);
			this.qValues.put(eatingPlace, q);
		}
		for (String sharedArea : this.sharedAreas.keySet()) {
			double q = RandomHelper.nextDoubleFromTo(Q_VALUE_INIT_FROM, Q_VALUE_INIT_TO);
			this.qValues.put(sharedArea, q);
		}
	}

	@Override
	public String selectAction(String currentLocation) {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		List<String> actions = new ArrayList<>(this.qValues.keySet());
		int index = -1;
		if (r < 1 - EPSILON) {
			double topValue = Double.NEGATIVE_INFINITY;
			List<String> ties = new ArrayList<>();
			for (String action : actions) {
				double qValue = this.qValues.get(action);
				if (qValue > topValue) {
					topValue = qValue;
					ties.clear();
					ties.add(action);
				} else if (qValue == topValue) {
					ties.add(action);
				}
			}
			index = RandomHelper.nextIntFromTo(0, ties.size() - 1);
			return ties.get(index);
		} else {
			index = RandomHelper.nextIntFromTo(0, actions.size() - 1);
			return actions.get(index);
		}
	}

	@Override
	public void updateLearning(String newState, double reward) {
		double lastQ = this.qValues.get(newState);
		double newQ = lastQ + LEARNING_RATE * (reward - lastQ);
		this.qValues.put(newState, newQ);
	}

	@Override
	public boolean containsState(String state) {
		return this.qValues.containsKey(state);
	}

}