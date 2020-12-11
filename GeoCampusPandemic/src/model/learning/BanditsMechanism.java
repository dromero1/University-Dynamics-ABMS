package model.learning;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import gis.GISPolygon;
import repast.simphony.random.RandomHelper;
import simulation.ParametersAdapter;

public class BanditsMechanism extends LearningMechanism {

	/**
	 * Q-values for states
	 */
	protected Map<String, Double> qValues;

	/**
	 * Epsilon parameter for epsilon-greedy action selection
	 */
	protected double epsilon;

	/**
	 * Learning rate for update rule
	 */
	protected double learningRate;

	/**
	 * Create a new Bandits mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public BanditsMechanism(Map<String, GISPolygon> teachingFacilities,
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
		for (String eatingPlace : this.eatingPlaces.keySet()) {
			double q = RandomHelper.nextDoubleFromTo(minInitQValue,
					maxInitQValue);
			this.qValues.put(eatingPlace, q);
		}
		for (String sharedArea : this.sharedAreas.keySet()) {
			double q = RandomHelper.nextDoubleFromTo(minInitQValue,
					maxInitQValue);
			this.qValues.put(sharedArea, q);
		}
	}

	/**
	 * Fix learning parameters
	 */
	@Override
	public void fixParameters() {
		this.epsilon = ParametersAdapter.getEpsilon();
		this.learningRate = ParametersAdapter.getLearningRate();
	}

	/**
	 * Select action
	 * 
	 * @param currentLocation Current location
	 */
	@Override
	public String selectAction(String currentLocation) {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		List<String> actions = new ArrayList<>(this.qValues.keySet());
		int index = -1;
		if (r < 1 - this.epsilon) {
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

	/**
	 * Update learning
	 * 
	 * @param newState New state
	 * @param reward   Reward
	 */
	@Override
	public void updateLearning(String newState, double reward) {
		double lastQ = this.qValues.get(newState);
		double newQ = lastQ + this.learningRate * (reward - lastQ);
		this.qValues.put(newState, newQ);
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