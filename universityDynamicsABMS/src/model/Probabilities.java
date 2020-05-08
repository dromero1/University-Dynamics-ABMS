package model;

import repast.simphony.random.RandomHelper;

public class Probabilities {

	public static final double MIN_LUNCH_TIME = 11.5;
	public static final double MAX_LUNCH_TIME = 14;
	public static final double MIN_LUNCH_DURATION = 0.5;
	public static final double MAX_LUNCH_DURATION = 1.5;

	public static double getRandomLunchTime() {
		return RandomHelper.nextDoubleFromTo(MIN_LUNCH_TIME, MAX_LUNCH_TIME);
	}

	public static double getRandomLunchDuration() {
		return RandomHelper.nextDoubleFromTo(MIN_LUNCH_DURATION, MAX_LUNCH_DURATION);
	}

}