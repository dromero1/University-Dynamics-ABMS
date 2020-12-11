package model.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gis.GISPolygon;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;
import simulation.ParametersAdapter;

public abstract class TDLearningMechanism extends LearningMechanism {

	/**
	 * Q-values for state-action pairs
	 */
	protected Map<String, List<Pair<String, Double>>> qValues;

	/**
	 * Last state
	 */
	protected String lastState;

	/**
	 * Last action
	 */
	protected String lastAction;

	/**
	 * Epsilon parameter for epsilon-greedy action selection
	 */
	protected double epsilon;

	/**
	 * Learning rate for update rule
	 */
	protected double learningRate;

	/**
	 * Discount factor for update rule
	 */
	protected double discountFactor;

	/**
	 * Create a new TD-learning mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public TDLearningMechanism(Map<String, GISPolygon> teachingFacilities,
			Map<String, GISPolygon> sharedAreas,
			Map<String, GISPolygon> eatingPlaces) {
		super(teachingFacilities, sharedAreas, eatingPlaces);
	}

	/**
	 * Initialize learning
	 */
	@Override
	public void init() {
		double minInitQValue = ParametersAdapter.getMinimumInitialQValue();
		double maxInitQValue = ParametersAdapter.getMaximumInitialQValue();
		this.qValues = new HashMap<>();
		// Teaching facilities to shared areas and eating places
		for (String teachingFacility : this.teachingFacilities.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String sharedArea : this.sharedAreas.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(minInitQValue,
						maxInitQValue);
				destinations.add(new Pair<>(sharedArea, q));
			}
			for (String eatingPlace : this.eatingPlaces.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(minInitQValue,
						maxInitQValue);
				destinations.add(new Pair<>(eatingPlace, q));
			}
			this.qValues.put(teachingFacility, destinations);
		}
		// Shared areas to eating places and shared areas
		for (String sharedArea : this.sharedAreas.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String eatingPlace : this.eatingPlaces.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(minInitQValue,
						maxInitQValue);
				destinations.add(new Pair<>(eatingPlace, q));
			}
			for (String nextSharedArea : this.sharedAreas.keySet()) {
				if (sharedArea.equals(nextSharedArea)) {
					continue;
				}
				double q = RandomHelper.nextDoubleFromTo(minInitQValue,
						maxInitQValue);
				destinations.add(new Pair<>(nextSharedArea, q));
			}
			this.qValues.put(sharedArea, destinations);
		}
		// Eating places to shared areas
		for (String eatingPlace : this.eatingPlaces.keySet()) {
			ArrayList<Pair<String, Double>> destinations = new ArrayList<>();
			for (String sharedArea : this.sharedAreas.keySet()) {
				double q = RandomHelper.nextDoubleFromTo(minInitQValue,
						maxInitQValue);
				destinations.add(new Pair<>(sharedArea, q));
			}
			this.qValues.put(eatingPlace, destinations);
		}
	}

	/**
	 * Fix learning parameters
	 */
	@Override
	public void fixParameters() {
		this.epsilon = ParametersAdapter.getEpsilon();
		this.learningRate = ParametersAdapter.getLearningRate();
		this.discountFactor = ParametersAdapter.getDiscountFactor();
	}

	/**
	 * Select action
	 * 
	 * @param currentLocation Current location
	 */
	@Override
	public String selectAction(String currentLocation) {
		this.lastState = currentLocation;
		List<Pair<String, Double>> destinations = this.qValues
				.get(currentLocation);
		Pair<String, Double> selectedDestination = null;
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		int index = -1;
		if (r < 1 - this.epsilon) {
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
			selectedDestination = ties.get(index);
		} else {
			index = RandomHelper.nextIntFromTo(0, destinations.size() - 1);
			selectedDestination = destinations.get(index);
		}
		this.lastAction = selectedDestination.getFirst();
		return this.lastAction;
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