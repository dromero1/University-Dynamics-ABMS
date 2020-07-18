package model;

import java.util.ArrayList;
import java.util.HashMap;
import cern.jet.random.Normal;
import gis.GISDensityMeter;
import gis.GISPolygon;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Random {

	/**
	 * Minimum lunch time (unit: hours). Reference: <pending>
	 */
	public static final double MIN_LUNCH_TIME = 11.5;

	/**
	 * Maximum lunch time (unit: hours). Reference: <pending>
	 */
	public static final double MAX_LUNCH_TIME = 14;

	/**
	 * Mean lunch duration (unit: hours). Reference: <pending>
	 */
	public static final double MEAN_LUNCH_DURATION = 0.66;

	/**
	 * Standard deviation of lunch duration (unit: hours). Reference: <pending>
	 */
	public static final double STD_LUNCH_DURATION = 0.16;

	/**
	 * Mean arrival shift (unit: hours). Reference: <pending>
	 */
	public static final double MEAN_ARRIVAL_SHIFT = 10.0 / 60;

	/**
	 * Standard deviation of arrival shift (unit: hours). Reference: <pending>
	 */
	public static final double STD_ARRIVAL_SHIFT = 5.0 / 60;

	/**
	 * Minimum walking speed (unit: meters/minute). Reference: <pending>
	 */
	public static final double MIN_WALKING_SPEED = 70;

	/**
	 * Maximum walking speed (unit: meters/minute). Reference: <pending>
	 */
	public static final double MAX_WALKING_SPEED = 100;

	/**
	 * Get random lunch time. Reference: <pending>
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
	 * Get random arrival shift. Reference: <pending>
	 */
	public static double getRandomArrivalShift() {
		Normal normal = RandomHelper.createNormal(MEAN_ARRIVAL_SHIFT, STD_ARRIVAL_SHIFT);
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
		return 2;
	}

	/**
	 * Get random work start time. Reference: <pending>
	 */
	public static double getRandomWorkStartTime() {
		return 8;
	}

	/**
	 * Get random work end time. Reference: <pending>
	 */
	public static double getRandomWorkEndTime() {
		return 18;
	}

	/**
	 * Get random geo-spatial polygon
	 * 
	 * @param polygons Map of polygons
	 */
	public static GISPolygon getRandomPolygon(HashMap<String, GISPolygon> polygons) {
		ArrayList<GISPolygon> polyList = new ArrayList<GISPolygon>();
		for (GISPolygon polygon : polygons.values()) {
			if (polygon instanceof GISDensityMeter) {
				GISDensityMeter densityMeter = (GISDensityMeter) polygon;
				if (!densityMeter.isActive()) {
					continue;
				}
			}
			polyList.add(polygon);
		}
		int index = RandomHelper.nextIntFromTo(0, polyList.size() - 1);
		return polyList.get(index);
	}

	/**
	 * Get random geo-spatial polygon based on weights
	 * 
	 * @param polygons Map of polygons
	 */
	public static GISPolygon getRandomPolygonWeightBased(HashMap<String, GISPolygon> polygons) {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		double cummulativeProbability = 0;
		for (GISPolygon polygon : polygons.values()) {
			if (polygon instanceof GISDensityMeter) {
				GISDensityMeter densityMeter = (GISDensityMeter) polygon;
				if (!densityMeter.isActive()) {
					continue;
				}
			}
			double weight = polygon.getWeight();
			cummulativeProbability += weight;
			if (r <= cummulativeProbability) {
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
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		double cummulativeProbability = 0;
		for (GISPolygon polygon : polygons.values()) {
			if (polygon instanceof GISDensityMeter) {
				GISDensityMeter densityMeter = (GISDensityMeter) polygon;
				if (!densityMeter.isActive()) {
					continue;
				}
			}
			double weight = polygon.getWorkWeight();
			cummulativeProbability += weight;
			if (r <= cummulativeProbability) {
				return polygon;
			}
		}
		return null;
	}

}