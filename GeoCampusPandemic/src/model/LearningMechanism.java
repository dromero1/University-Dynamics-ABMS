package model;

import java.util.HashMap;
import gis.GISPolygon;

public abstract class LearningMechanism {

	/**
	 * Teaching facilities
	 */
	protected HashMap<String, GISPolygon> teachingFacilities;

	/**
	 * Shared areas
	 */
	protected HashMap<String, GISPolygon> sharedAreas;

	/**
	 * Eating places
	 */
	protected HashMap<String, GISPolygon> eatingPlaces;

	/**
	 * Create a new learning mechanism
	 * 
	 * @param teachingFacilities Teaching facilities
	 * @param sharedAreas        Shared areas
	 * @param eatingPlaces       Eating places
	 */
	public LearningMechanism(HashMap<String, GISPolygon> teachingFacilities, HashMap<String, GISPolygon> sharedAreas,
			HashMap<String, GISPolygon> eatingPlaces) {
		this.teachingFacilities = teachingFacilities;
		this.sharedAreas = sharedAreas;
		this.eatingPlaces = eatingPlaces;
		init();
	}

	/**
	 * Initialize learning
	 */
	public abstract void init();

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