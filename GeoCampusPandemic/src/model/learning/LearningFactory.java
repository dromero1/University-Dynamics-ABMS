package model.learning;

import java.util.HashMap;
import gis.GISPolygon;

public class LearningFactory {

	/**
	 * Instantiate a new learning mechanism
	 * 
	 * @param learningStyle Learning style
	 */
	public static LearningMechanism makeLearningMechanism(LearningStyle learningStyle,
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