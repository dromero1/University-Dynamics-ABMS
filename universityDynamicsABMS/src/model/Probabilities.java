package model;

import cern.jet.random.Normal;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;

public class Probabilities {

	public static final double MIN_LUNCH_TIME = 11.5;
	public static final double MAX_LUNCH_TIME = 14;
	public static final double MEAN_LUNCH_DURATION = 40;
	public static final double STD_LUNCH_DURATION = 10;

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
	
}