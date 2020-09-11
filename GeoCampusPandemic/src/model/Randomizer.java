package model;

import java.util.ArrayList;
import java.util.HashMap;
import cern.jet.random.Binomial;
import cern.jet.random.Gamma;
import cern.jet.random.Normal;
import gis.GISPolygon;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import util.TickConverter;

public class Randomizer {

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
	 * Minimum staffer arrival time (unit: hours). Reference: <pending>
	 */
	public static final double MIN_STAFFER_ARRIVAL_TIME = 7;

	/**
	 * Maximum staffer arrival time (unit: hours). Reference: <pending>
	 */
	public static final double MAX_STAFFER_ARRIVAL_TIME = 9;
	
	/**
	 * Minimum staffer departure time (unit: hours). Reference: <pending>
	 */
	public static final double MIN_STAFFER_DEPARTURE_TIME = 17;

	/**
	 * Maximum staffer departure time (unit: hours). Reference: <pending>
	 */
	public static final double MAX_STAFFER_DEPARTURE_TIME = 19;

	/**
	 * Minimum student arrival time (unit: hours). Reference: <pending>
	 */
	public static final double MIN_STUDENT_ARRIVAL_TIME = 6;

	/**
	 * Maximum student arrival time (unit: hours). Reference: <pending>
	 */
	public static final double MAX_STUDENT_ARRIVAL_TIME = 17;
	
	/**
	 * Minimum student departure time (unit: hours). Reference: <pending>
	 */
	public static final double MIN_STUDENT_DEPARTURE_TIME = 17;

	/**
	 * Maximum student departure time (unit: hours). Reference: <pending>
	 */
	public static final double MAX_STUDENT_DEPARTURE_TIME = 19;

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
	 * Infection alpha parameter. Reference: <pending>
	 */
	public static final double INFECTION_ALPHA = 2.11;

	/**
	 * Infection beta parameter. Reference: <pending>
	 */
	public static final double INFECTION_BETA = 1.3;

	/**
	 * Infection minimum parameter (unit: days). Reference: <pending>
	 */
	public static final double INFECTION_MIN = -2.4;

	/**
	 * Discharge alpha parameter. Reference: <pending>
	 */
	public static final double DISCHARGE_ALPHA = 1.99;

	/**
	 * Discharge beta parameter. Reference: <pending>
	 */
	public static final double DISCHARGE_BETA = 7.77;

	/**
	 * Incubation period mean parameter (unit: days). Reference: <pending>
	 */
	public static final double MEAN_INCUBATION_PERIOD = 5.52;

	/**
	 * Incubation period standard deviation parameter (unit: days). Reference:
	 * <pending>
	 */
	public static final double STD_INCUBATION_PERIOD = 2.41;

	/**
	 * Groups to enroll trials parameter (unit: groups). Reference: <pending>
	 */
	public static final int TRIALS_GROUPS_TO_ENROLL = 7;

	/**
	 * Groups to enroll success probability parameter (unit: probability).
	 * Reference: <pending>
	 */
	public static final double SUCCESS_PROBABILITY_GROUPS_TO_ENROLL = 0.71;

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
		Binomial binomial = RandomHelper.createBinomial(TRIALS_GROUPS_TO_ENROLL, SUCCESS_PROBABILITY_GROUPS_TO_ENROLL);
		return binomial.nextInt();
	}

	/**
	 * Get random staffer's arrival time. Reference: <pending>
	 */
	public static double getRandomStafferArrivalTime() {
		return RandomHelper.nextDoubleFromTo(MIN_STAFFER_ARRIVAL_TIME, MAX_STAFFER_ARRIVAL_TIME);
	}

	/**
	 * Get random staffer's departure time. Reference: <pending>
	 */
	public static double getRandomStafferDepartureTime() {
		return RandomHelper.nextDoubleFromTo(MIN_STAFFER_DEPARTURE_TIME, MAX_STAFFER_DEPARTURE_TIME);
	}

	/**
	 * Get random student's arrival time. Reference: <pending>
	 */
	public static double getRandomStudentArrivalTime() {
		return RandomHelper.nextDoubleFromTo(MIN_STUDENT_ARRIVAL_TIME, MAX_STUDENT_ARRIVAL_TIME);
	}
	
	/**
	 * Get random student's departure time. Reference: <pending>
	 */
	public static double getRandomStudentDepartureTime() {
		return RandomHelper.nextDoubleFromTo(MIN_STUDENT_DEPARTURE_TIME, MAX_STUDENT_DEPARTURE_TIME);
	}

	/**
	 * Get random incubation period (unit: days). Reference: <pending>
	 */
	public static double getRandomIncubationPeriod() {
		double t = Math.pow(MEAN_INCUBATION_PERIOD, 2) + Math.pow(STD_INCUBATION_PERIOD, 2);
		double mu = Math.log(Math.pow(MEAN_INCUBATION_PERIOD, 2) / Math.sqrt(t));
		double sigma = Math.log(t / Math.pow(MEAN_INCUBATION_PERIOD, 2));
		Normal normal = RandomHelper.createNormal(mu, sigma);
		double y = normal.nextDouble();
		return Math.exp(y);
	}

	/**
	 * Get random patient type. Reference: <pending>
	 */
	public static PatientType getRandomPatientType() {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		if (r < 0.3) {
			return PatientType.NO_SYMPTOMS;
		} else if (r < 0.85) {
			return PatientType.MODERATE_SYMPTOMS;
		} else if (r < 0.95) {
			return PatientType.SEVERE_SYMPTOMS;
		} else {
			return PatientType.CRITICAL_SYMPTOMS;
		}
	}

	/**
	 * Is the patient going to die? Reference: <pending>
	 * 
	 * @param patientType Patient type
	 */
	public static boolean isGoingToDie(PatientType patientType) {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		switch (patientType) {
		case SEVERE_SYMPTOMS:
			return r < 0.15;
		case CRITICAL_SYMPTOMS:
			return r < 0.5;
		default:
			return false;
		}
	}

	/**
	 * Is the citizen getting exposed? Reference: <pending>
	 * 
	 * @param incubationDiff Incubation difference
	 */
	public static boolean isGettingExposed(double incubationDiff) {
		double r = RandomHelper.nextDoubleFromTo(0, 1);
		Gamma gamma = RandomHelper.createGamma(INFECTION_ALPHA, 1.0 / INFECTION_BETA);
		double days = TickConverter.ticksToDays(incubationDiff);
		if (days < INFECTION_MIN) {
			return false;
		}
		double p = gamma.pdf(days - INFECTION_MIN);
		return r < p;
	}

	/**
	 * Get random time to discharge (unit: days). Reference: <pending>
	 */
	public static double getRandomTimeToDischarge() {
		Gamma gamma = RandomHelper.createGamma(DISCHARGE_ALPHA, 1.0 / DISCHARGE_BETA);
		return gamma.nextDouble();
	}

	/**
	 * Get random geo-spatial polygon
	 * 
	 * @param polygons Map of polygons
	 */
	public static GISPolygon getRandomPolygon(HashMap<String, GISPolygon> polygons) {
		ArrayList<GISPolygon> polyList = new ArrayList<>();
		for (GISPolygon polygon : polygons.values()) {
			if (!polygon.isActive()) {
				continue;
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
			if (!polygon.isActive()) {
				continue;
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
			if (!polygon.isActive()) {
				continue;
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