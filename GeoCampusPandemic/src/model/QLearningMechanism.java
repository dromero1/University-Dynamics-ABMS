package model;

import java.util.ArrayList;
import java.util.HashMap;
import gis.GISPolygon;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;

public class QLearningMechanism extends LearningMechanism {

	/**
	 * Q-values for state-action pairs
	 */
	private HashMap<String, ArrayList<Pair<String, Double>>> qValues;

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
	private static final double EPSILON = 0.9;

	/**
	 * Create a new learning mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public QLearningMechanism(HashMap<String, GISPolygon> teachingFacilities, HashMap<String, GISPolygon> sharedAreas,
			HashMap<String, GISPolygon> eatingPlaces) {
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
				double q = RandomHelper.nextDoubleFromTo(0, 1.0);
				destinations.add(new Pair<>(sharedArea, q));
			}
			for (String eatingPlace : this.eatingPlaces.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(0, 1.0);
				destinations.add(new Pair<>(eatingPlace, q));
			}
			this.qValues.put(teachingFacility, destinations);
		}
		// Shared areas to eating places
		for (String sharedArea : this.sharedAreas.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String eatingPlace : this.eatingPlaces.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(0, 1.0);
				destinations.add(new Pair<>(eatingPlace, q));
			}
			this.qValues.put(sharedArea, destinations);
		}
		// Eating places to shared areas
		for (String eatingPlace : this.eatingPlaces.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String sharedArea : this.sharedAreas.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(0, 1.0);
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
		ArrayList<Pair<String, Double>> destinations = this.qValues.get(currentLocation);
		Pair<String, Double> selectedDestination = null;
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		int index = -1;
		if (r < EPSILON) {
			double topValue = -1.0;
			ArrayList<Pair<String, Double>> ties = new ArrayList<>();
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