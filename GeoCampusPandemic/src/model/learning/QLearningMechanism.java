package model.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gis.GISPolygon;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;

public class QLearningMechanism extends LearningMechanism {

	/**
	 * Q-values for state-action pairs
	 */
	private Map<String, List<Pair<String, Double>>> qValues;

	/**
	 * Last state
	 */
	private String lastState;

	/**
	 * Last action
	 */
	private String lastAction;

	/**
	 * Epsilon parameter for epsilon-greedy action selection
	 */
	private static final double EPSILON = 0.2;

	/**
	 * Learning rate for update rule
	 */
	private static final double LEARNING_RATE = 0.5;

	/**
	 * Discount factor for update rule
	 */
	private static final double DISCOUNT_FACTOR = 0.8;

	/**
	 * Create a new learning mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public QLearningMechanism(Map<String, GISPolygon> teachingFacilities, Map<String, GISPolygon> sharedAreas,
			Map<String, GISPolygon> eatingPlaces) {
		super(teachingFacilities, sharedAreas, eatingPlaces);
	}

	/**
	 * Initialize learning
	 */
	@Override
	public void init() {
		this.qValues = new HashMap<>();
		// Teaching facilities to shared areas and eating places
		for (String teachingFacility : this.teachingFacilities.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String sharedArea : this.sharedAreas.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(-1.0, 1.0);
				destinations.add(new Pair<>(sharedArea, q));
			}
			for (String eatingPlace : this.eatingPlaces.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(-1.0, 1.0);
				destinations.add(new Pair<>(eatingPlace, q));
			}
			this.qValues.put(teachingFacility, destinations);
		}
		// Shared areas to eating places
		for (String sharedArea : this.sharedAreas.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String eatingPlace : this.eatingPlaces.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(-1.0, 1.0);
				destinations.add(new Pair<>(eatingPlace, q));
			}
			this.qValues.put(sharedArea, destinations);
		}
		// Eating places to shared areas
		for (String eatingPlace : this.eatingPlaces.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String sharedArea : this.sharedAreas.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(-1.0, 1.0);
				destinations.add(new Pair<>(sharedArea, q));
			}
			this.qValues.put(eatingPlace, destinations);
		}
	}

	/**
	 * Select action
	 * 
	 * @param currentLocation Current location
	 */
	@Override
	public String selectAction(String currentLocation) {
		this.lastState = currentLocation;
		List<Pair<String, Double>> destinations = this.qValues.get(currentLocation);
		Pair<String, Double> selectedDestination = null;
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		int index = -1;
		if (r < 1 - EPSILON) {
			double topValue = Double.NEGATIVE_INFINITY;
			List<Pair<String, Double>> ties = new ArrayList<>();
			for (Pair<String, Double> destination : destinations) {
				double qValue = destination.getSecond();
				if (qValue > topValue) {
					topValue = qValue;
					ties.clear();
					ties.add(destination);
				} else if (qValue == topValue) {
					ties.add(destination);
				}
			}
			index = RandomHelper.nextIntFromTo(0, ties.size() - 1);
		} else {
			index = RandomHelper.nextIntFromTo(0, destinations.size() - 1);
		}
		selectedDestination = destinations.get(index);
		this.lastAction = selectedDestination.getFirst();
		return this.lastAction;
	}

	/**
	 * Update learning
	 * 
	 * @param newState New state
	 * @param reward   Reward
	 */
	@Override
	public void updateLearning(String newState, double reward) {
		if (this.lastState != null) {
			double maxQ = Double.NEGATIVE_INFINITY;
			List<Pair<String, Double>> newStateActionValues = this.qValues.get(newState);
			for (Pair<String, Double> stateActionValue : newStateActionValues) {
				double q = stateActionValue.getSecond();
				if (q > maxQ) {
					maxQ = q;
				}
			}
			List<Pair<String, Double>> lastStateActionValues = this.qValues.get(this.lastState);
			for (int i = 0; i < lastStateActionValues.size(); i++) {
				Pair<String, Double> stateActionValue = lastStateActionValues.get(i);
				String action = stateActionValue.getFirst();
				if (action.equals(this.lastAction)) {
					double q = stateActionValue.getSecond();
					q = q + LEARNING_RATE * (reward + DISCOUNT_FACTOR * maxQ - q);
					stateActionValue.setSecond(q);
					lastStateActionValues.set(i, stateActionValue);
				}
			}
			this.qValues.put(this.lastState, lastStateActionValues);
		}
	}

	/**
	 * Returns true if this learning mechanism contains the specified state
	 * 
	 * @param state State
	 */
	@Override
	public boolean containsState(String state) {
		return this.qValues.containsKey(state);
	}

}