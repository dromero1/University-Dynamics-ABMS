package util;

import repast.simphony.util.collections.Pair;

public class TickConverter {

	public static final double TICKS_PER_MINUTE = 1.0 / 60;
	public static final double TICKS_PER_WEEK = 168;
	public static final double TICKS_PER_DAY = 24;

	public static double dayTimeToTicks(int day, double time) {
		return (day - 1) * TICKS_PER_DAY + time;
	}

	public static Pair<Double, Double> tickToDayTime(double tick) {
		// TODO Check this method
		double day = Math.floor(((tick / TICKS_PER_DAY) % 7) + 1);
		double hours = (((tick / TICKS_PER_DAY) % 7) + 1 - day) * TICKS_PER_DAY;
		return new Pair<Double, Double>(day, hours);
	}

	public static double minutesToTicks(double minutes) {
		return minutes * TICKS_PER_MINUTE;
	}
	
}