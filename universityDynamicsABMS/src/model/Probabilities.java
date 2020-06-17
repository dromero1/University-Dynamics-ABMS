package model;

import java.util.ArrayList;
import java.util.Collections;
import cern.jet.random.Normal;
import gis.GISDensityMeter;
import gis.GISPolygon;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Probabilities {

	public static final double MIN_LUNCH_TIME = 11; // [hour]
	public static final double MAX_LUNCH_TIME = 16; // [hour]
	public static final double MEAN_LUNCH_DURATION = 0.66; // [hour]
	public static final double STD_LUNCH_DURATION = 0.16; // [hour]
	public static final double MIN_WALKING_SPEED = 70; // [m]/[min]
	public static final double MAX_WALKING_SPEED = 100; // [m]/[min]

	public static double getRandomLunchTime() {
		return RandomHelper.nextDoubleFromTo(MIN_LUNCH_TIME, MAX_LUNCH_TIME);
	}

	public static double getRandomLunchDuration() {
		Normal normal = RandomHelper.createNormal(MEAN_LUNCH_DURATION, STD_LUNCH_DURATION);
		return normal.nextDouble();
	}

	public static boolean getRandomVehicleUsage() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		double vehicleUsageRatio = simParams.getDouble("vehicleUsageRatio");
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		return r < vehicleUsageRatio;
	}

	public static double getRandomWalkingSpeed() {
		return RandomHelper.nextDoubleFromTo(MIN_WALKING_SPEED, MAX_WALKING_SPEED);
	}

	public static GISPolygon getRandomPolygonWeightBased(Object[] polygons) {
		ArrayList<GISDensityMeter> densityPolygons = new ArrayList<GISDensityMeter>();
		for (int i = 0; i < polygons.length; i++) {
			densityPolygons.add((GISDensityMeter) polygons[i]);
		}
		Collections.sort(densityPolygons);
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		for (GISDensityMeter densityPolygon : densityPolygons) {
			double weight = densityPolygon.getWeight();
			if (r <= weight) {
				return densityPolygon;
			}
		}
		return null;
	}

}