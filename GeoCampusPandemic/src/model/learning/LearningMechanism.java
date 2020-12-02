package model.learning;

import java.util.Map;
import gis.GISPolygon;

public abstract class LearningMechanism {

	/**
	 * Teaching facilities
	 */
	protected Map<String, GISPolygon> teachingFacilities;

	/**
	 * Shared areas
	 */
	protected Map<String, GISPolygon> sharedAreas;

	/**
	 * Eating places
	 */
	protected Map<String, GISPolygon> eatingPlaces;

	/**
	 * Create a new learning mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public LearningMechanism(Map<String, GISPolygon> teachingFacilities,
			Map<String, GISPolygon> sharedAreas,
			Map<String, GISPolygon> eatingPlaces) {
		this.teachingFacilities = teachingFacilities;
		this.sharedAreas = sharedAreas;
		this.eatingPlaces = eatingPlaces;
		init();
		fixParameters();
	}

	/**
	 * Initialize learning
	 */
	public abstract void init();

	/**
	 * Fix learning parameters
	 */
	public abstract void fixParameters();

	/**
	 * Select action
	 * 
	 * @param currentLocation Current location
	 */
	public abstract String selectAction(String currentLocation);

	/**
	 * Update learning
	 * 
	 * @param newState New state
	 * @param reward   Reward
	 */
	public abstract void updateLearning(String newState, double reward);

	/**
	 * Returns true if this learning mechanism contains the specified state
	 * 
	 * @param state State
	 */
	public abstract boolean containsState(String state);

}