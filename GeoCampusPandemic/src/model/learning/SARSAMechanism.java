package model.learning;

import java.util.List;
import java.util.Map;
import gis.GISPolygon;
import repast.simphony.util.collections.Pair;

public class SARSAMechanism extends TDLearningMechanism {

	/**
	 * Create a new SARSA mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public SARSAMechanism(Map<String, GISPolygon> teachingFacilities, Map<String, GISPolygon> sharedAreas,
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
			double qPrime = 0.0;
			String newAction = selectAction(newState);
			List<Pair<String, Double>> newStateActionValues = this.qValues.get(newState);
			for (Pair<String, Double> stateActionValue : newStateActionValues) {
				String action = stateActionValue.getFirst();
				if (action.equals(newAction)) {
					qPrime = stateActionValue.getSecond();
					break;
				}
			}
			List<Pair<String, Double>> lastStateActionValues = this.qValues.get(this.lastState);
			for (int i = 0; i < lastStateActionValues.size(); i++) {
				Pair<String, Double> stateActionValue = lastStateActionValues.get(i);
				String action = stateActionValue.getFirst();
				if (action.equals(this.lastAction)) {
					double q = stateActionValue.getSecond();
					q = q + LEARNING_RATE * (reward + DISCOUNT_FACTOR * qPrime - q);
					stateActionValue.setSecond(q);
					lastStateActionValues.set(i, stateActionValue);
				}
			}
			this.qValues.put(this.lastState, lastStateActionValues);
		}
	}

}