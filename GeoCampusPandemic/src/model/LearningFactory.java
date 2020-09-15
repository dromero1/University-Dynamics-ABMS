package model;

import java.util.HashMap;
import gis.GISPolygon;

public class LearningFactory {

	/**
	 * Q-learning algorithm
	 */
	private static final String Q_LEARNING = "Q-learning";

	/**
	 * Instantiate a new learning mechanism
	 * 
	 * @param learningStyle Learning style
	 */
	public static LearningMechanism makeLearningMechanism(String learningStyle,
			HashMap<String, GISPolygon> teachingFacilities, HashMap<String, GISPolygon> sharedAreas,
			HashMap<String, GISPolygon> eatingPlaces) {
		switch (learningStyle) {
		case Q_LEARNING:
			return new QLearningMechanism(teachingFacilities, sharedAreas, eatingPlaces);
		default:
			break;
		}
		return null;
	}

}