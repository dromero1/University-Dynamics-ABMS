package model.learning;

import java.util.List;
import java.util.Map;
import gis.GISPolygon;
import repast.simphony.util.collections.Pair;

public class QLearningMechanism extends TDLearningMechanism {

	/**
	 * Create a new Q-learning mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public QLearningMechanism(Map<String, GISPolygon> teachingFacilities,
			Map<String, GISPolygon> sharedAreas,
			Map<String, GISPolygon> eatingPlaces) {
		super(teachingFacilities, sharedAreas, eatingPlaces);
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
			List<Pair<String, Double>> newStateActionValues = this.qValues
					.get(newState);
			for (Pair<String, Double> stateActionValue : newStateActionValues) {
				double q = stateActionValue.getSecond();
				if (q > maxQ) {
					maxQ = q;
				}
			}
			List<Pair<String, Double>> lastStateActionValues = this.qValues
					.get(this.lastState);
			for (int i = 0; i < lastStateActionValues.size(); i++) {
				Pair<String, Double> stateActionValue = lastStateActionValues
						.get(i);
				String action = stateActionValue.getFirst();
				if (action.equals(this.lastAction)) {
					double q = stateActionValue.getSecond();
					q = q + this.learningRate
							* (reward + this.discountFactor * maxQ - q);
					stateActionValue.setSecond(q);
					lastStateActionValues.set(i, stateActionValue);
				}
			}
			this.qValues.put(this.lastState, lastStateActionValues);
		}
	}

}