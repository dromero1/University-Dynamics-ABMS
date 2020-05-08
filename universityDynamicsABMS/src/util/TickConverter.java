package util;

public class TickConverter {

	public static final double TICKS_PER_WEEK = 168;
	public static final double TICKS_PER_DAY = 24;

	public static double dayTimeToTicks(int day, double time) {
		return (day - 1) * TICKS_PER_DAY + time;
	}

	public static double[] tickToDayTime(double tick) {
		// TODO Check this method
		double day = Math.floor(((tick / TICKS_PER_DAY) % 7) + 1);
		double hours = (((tick / TICKS_PER_DAY) % 7) + 1 - day) * TICKS_PER_DAY;
		double[] dayTime = { day, hours };
		return dayTime;
	}

}