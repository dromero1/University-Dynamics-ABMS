package model.learning;

import java.util.Map;
import gis.GISPolygon;

public final class LearningFactory {

	/**
	 * Private constructor
	 */
	private LearningFactory() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Instantiate a new learning mechanism
	 * 
	 * @param learningStyle Learning style
	 */
	public static LearningMechanism makeLearningMechanism(LearningStyle learningStyle,
			Map<String, GISPolygon> teachingFacilities, Map<String, GISPolygon> sharedAreas,
			Map<String, GISPolygon> eatingPlaces) {
		if (learningStyle == LearningStyle.Q_LEARNING) {
			return new QLearningMechanism(teachingFacilities, sharedAreas, eatingPlaces);
		} else if (learningStyle == LearningStyle.SARSA) {
			return new SARSAMechanism(teachingFacilities, sharedAreas, eatingPlaces);
		} else if (learningStyle == LearningStyle.BANDITS) {
			return new BanditsMechanism(teachingFacilities, sharedAreas, eatingPlaces);
		} else {
			return null;
		}
	}

}