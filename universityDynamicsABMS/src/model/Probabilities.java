package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import cern.jet.random.Normal;
import gis.GISDensityMeter;
import gis.GISPolygon;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.util.collections.Pair;

public class Probabilities {

	/**
	 * Minimum lunch time (unit: hours). Reference: <pending>
	 */
	public static final double MIN_LUNCH_TIME = 11;

	/**
	 * Maximum lunch time (unit: hours). Reference: <pending>
	 */
	public static final double MAX_LUNCH_TIME = 16;

	/**
	 * Mean lunch duration (unit: hours). Reference: <pending>
	 */
	public static final double MEAN_LUNCH_DURATION = 0.66;

	/**
	 * Standard deviation of lunch duration (unit: hours). Reference: <pending>
	 */
	public static final double STD_LUNCH_DURATION = 0.16;

	/**
	 * Minimum walking speed (unit: meters/minute). Reference: <pending>
	 */
	public static final double MIN_WALKING_SPEED = 70;

	/**
	 * Maximum walking speed (unit: meters/minute). Reference: <pending>
	 */
	public static final double MAX_WALKING_SPEED = 100;

	/**
	 * Get random lunch time
	 */
	public static double getRandomLunchTime() {
		return RandomHelper.nextDoubleFromTo(MIN_LUNCH_TIME, MAX_LUNCH_TIME);
	}

	/**
	 * Get random lunch duration. Reference: <pending>
	 */
	public static double getRandomLunchDuration() {
		Normal normal = RandomHelper.createNormal(MEAN_LUNCH_DURATION, STD_LUNCH_DURATION);
		return normal.nextDouble();
	}

	/**
	 * Get random vehicle usage. Reference: <pending>
	 */
	public static boolean getRandomVehicleUsage() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		double vehicleUsageRatio = simParams.getDouble("vehicleUsageRatio");
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r < vehicleUsageRatio;
	}

	/**
	 * Get random walking speed. Reference: <pending>
	 */
	public static double getRandomWalkingSpeed() {
		return RandomHelper.nextDoubleFromTo(MIN_WALKING_SPEED, MAX_WALKING_SPEED);
	}

	/**
	 * Get random number of groups to enroll to. Reference: <pending>
	 */
	public static int getRandomGroupsToEnrollTo() {
		return 3;
	}

	/**
	 * Get random work start time. Reference: <pending>
	 */
	public static double getRandomWorkStartTime() {
		return 7;
	}

	/**
	 * Get random work end time. Reference: <pending>
	 */
	public static double getRandomWorkEndTime() {
		return 17;
	}

	/**
	 * Get random geo-spatial polygon
	 * 
	 * @param polygons Map of polygons
	 */
	public static GISPolygon getRandomPolygon(HashMap<String, GISPolygon> polygons) {
		Object[] locations = polygons.values().toArray();
		int i = RandomHelper.nextIntFromTo(0, locations.length - 1);
		return (GISPolygon) locations[i];
	}

	/**
	 * Get random geo-spatial polygon based on weights
	 * 
	 * @param polygons Map of polygons
	 */
	public static GISPolygon getRandomPolygonWeightBased(HashMap<String, GISPolygon> polygons) {
		ArrayList<GISPolygon> polyList = new ArrayList<GISPolygon>();
		for (GISPolygon polygon : polygons.values()) {
			polyList.add(polygon);
		}
		Collections.sort(polyList, new Comparator<GISPolygon>() {
			@Override
			public int compare(GISPolygon poly1, GISPolygon poly2) {
				if (poly1.getWeight() > poly2.getWeight()) {
					return 1;
				} else if (poly1.getWeight() < poly2.getWeight()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		for (GISPolygon polygon : polyList) {
			double weight = polygon.getWeight();
			if (r <= weight) {
				return polygon;
			}
		}
		return null;
	}

	/**
	 * Get random geo-spatial polygon based on work weights
	 * 
	 * @param polygons Map of polygons
	 */
	public static GISPolygon getRandomPolygonWorkWeightBased(HashMap<String, GISPolygon> polygons) {
		ArrayList<GISPolygon> polyList = new ArrayList<GISPolygon>();
		for (GISPolygon polygon : polygons.values()) {
			polyList.add(polygon);
		}
		Collections.sort(polyList, new Comparator<GISPolygon>() {
			@Override
			public int compare(GISPolygon poly1, GISPolygon poly2) {
				if (poly1.getWorkWeight() > poly2.getWorkWeight()) {
					return 1;
				} else if (poly1.getWorkWeight() < poly2.getWorkWeight()) {
					return -1;
				} else {
					return 0;
				}
			}
		});
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		for (GISPolygon polygon : polyList) {
			double weight = polygon.getWorkWeight();
			if (r <= weight) {
				return polygon;
			}
		}
		return null;
	}
	
	/**
	 * Get random geo-spatial polygon based on the k-armed bandit problem. Solution
	 * is based on the epsilon-greedy action selection algorithm.
	 * 
	 * @param polygons             Map of polygons
	 * @param actionValueEstimates Estimates of action values (Q_t)
	 */
	public static GISPolygon getRandomPolygonBanditBased(HashMap<String, GISPolygon> polygons,
			HashMap<String, Pair<Double, Integer>> actionValueEstimates) {
		ArrayList<GISPolygon> actionablePolygons = new ArrayList<GISPolygon>();
		for (GISPolygon polygon : polygons.values()) {
			actionablePolygons.add(polygon);
		}
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		double epsilon = 0.1;
		if (r < 1 - epsilon) {
			double top = Double.NEGATIVE_INFINITY;
			ArrayList<String> ties = new ArrayList<String>();
			for (GISPolygon actionablePolygon : actionablePolygons) {
				String id = actionablePolygon.getId();
				Pair<Double, Integer> estimation = actionValueEstimates.get(id);
				double Q = estimation.getFirst();
				if (Q > top) {
					top = Q;
					ties.clear();
					ties.add(id);
				} else if (Q == top) {
					ties.add(id);
				}
			}
			Collections.shuffle(ties);
			String selectedId = ties.get(0);
			return polygons.get(selectedId);
		} else {
			return getRandomPolygon(polygons);
		}
	}

}