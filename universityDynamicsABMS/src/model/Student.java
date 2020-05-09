package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import gis.GISLimbo;
import gis.GISPolygon;
import gis.GISTeachingFacility;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class Student {

	/**
	 * Arrival, departure, and leaving delay
	 */
	private static final double STEP_DELAY = 1.0 / 6;

	/**
	 * Academic schedule
	 */
	private Schedule schedule;

	/**
	 * Learning flag. Determines whether the student is currently busy learning or
	 * not.
	 */
	private boolean learning;

	/**
	 * Time to lunch
	 */
	private double lunchTime;

	/**
	 * Time spend eating lunch
	 */
	private double lunchDuration;

	/**
	 * Reference to geography projection
	 */
	private Geography<Object> geography;

	/**
	 * Reference to simulation builder
	 */
	private SimulationBuilder contextBuilder;

	/**
	 * Create a new student agent
	 * 
	 * @param geography      Reference to geography projection
	 * @param contextBuilder Reference to the simulation builder
	 */
	public Student(Geography<Object> geography, SimulationBuilder contextBuilder) {
		this.geography = geography;
		this.contextBuilder = contextBuilder;
		this.learning = false;
		this.lunchTime = Probabilities.getRandomLunchTime();
		this.lunchDuration = Probabilities.getRandomLunchDuration();
		vanishToLimbo();
	}

	/**
	 * Assign an academic schedule
	 * 
	 * @param schedule Academic schedule
	 */
	public void setSchedule(Schedule schedule) {
		this.schedule = schedule;
	}

	/**
	 * Attend an academic activity
	 * 
	 * @param teachingFacilityId Id of the teaching facility
	 */
	public void attendActivity(String teachingFacilityId) {
		HashMap<String, GISTeachingFacility> teachingFacilities = this.contextBuilder.getTeachingFacilities();
		if (teachingFacilities.containsKey(teachingFacilityId)) {
			// Move to teaching facility
			GISTeachingFacility teachingFacility = teachingFacilities.get(teachingFacilityId);
			moveToPolygon(teachingFacility);
			// Update learning flag
			this.learning = true;
		}
	}

	/**
	 * Leave an academic activity. The student determines what to do next. If he/she
	 * has an activity in less than STEP_DELAY ticks he/she prefers to go there. In
	 * the other case, the student goes to have fun.
	 */
	public void leaveActivity() {
		// Update learning flag
		this.learning = false;
		// Determine what to do next
		double tick = RepastEssentials.GetTickCount();
		double[] dayTime = TickConverter.tickToDayTime(tick);
		double day = dayTime[0];
		double hour = dayTime[1];
		AcademicActivity nextActivity = this.schedule.getNextAcademicActivity(day, hour);
		if (nextActivity != null) {
			double delta = nextActivity.getStartTime() - hour;
			if (delta < STEP_DELAY) {
				attendActivity(nextActivity.getTeachingFacilityId());
			} else {
				EventScheduler eventScheduler = EventScheduler.getInstance();
				eventScheduler.scheduleRecurringEvent(STEP_DELAY, this, TickConverter.TICKS_PER_WEEK, "haveFun");
			}
		}
	}

	/**
	 * Go have lunch at a designated eating place
	 */
	public void haveLunch() {
		// Move to random eating place
		Object[] eatingPlaces = this.contextBuilder.getEatingPlaces().values().toArray();
		moveToRandomPolygon(eatingPlaces);
	}

	/**
	 * Go have fun at a shared area
	 */
	public void haveFun() {
		// Move to random shared area
		Object[] sharedAreas = this.contextBuilder.getSharedAreas().values().toArray();
		moveToRandomPolygon(sharedAreas);
	}

	/**
	 * Move to random in-out spot and vanish to limbo
	 */
	public void returnHome() {
		moveToRandomInOutSpot();
		EventScheduler eventScheduler = EventScheduler.getInstance();
		eventScheduler.scheduleRecurringEvent(STEP_DELAY, this, TickConverter.TICKS_PER_WEEK, "vanishToLimbo");
	}

	/**
	 * Move to random in-out spot
	 */
	public void moveToRandomInOutSpot() {
		Object[] inOuts = this.contextBuilder.getInOuts().values().toArray();
		moveToRandomPolygon(inOuts);
	}

	/**
	 * Vanish to limbo. 'Limbo' emulates what's off campus.
	 */
	public void vanishToLimbo() {
		GISLimbo limbo = this.contextBuilder.getLimbo();
		moveToPolygon(limbo);
	}

	/**
	 * Plan weekly events
	 */
	public void planWeeklyEvents() {
		// Schedule simple events
		scheduleArrivalsAndDepartures();
		scheduleLunch();
		// Schedule activity timetable
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (Group group : this.schedule.getGroups()) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				// Schedule activity attendance
				int day = activity.getDay();
				double startTime = activity.getStartTime();
				String teachingFacilityId = activity.getTeachingFacilityId();
				double ticksToEvent = TickConverter.dayTimeToTicks(day, startTime);
				eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
						"attendActivity", teachingFacilityId);
				// Schedule leaving the activity
				double endTime = activity.getEndTime();
				ticksToEvent = TickConverter.dayTimeToTicks(day, endTime);
				eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
						"leaveActivity");
			}
		}
	}

	/**
	 * Whether or not the student is currently busy learning
	 */
	public boolean isLearning() {
		return learning;
	}

	/**
	 * Schedule arrivals and departures
	 */
	private void scheduleArrivalsAndDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			AcademicActivity firstActivity = schedule.getFirstAcademicActivityInDay(day);
			double startTime = firstActivity.getStartTime() - STEP_DELAY;
			double ticksToEvent = TickConverter.dayTimeToTicks(day, startTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
					"moveToRandomInOutSpot");
			AcademicActivity lastActivity = schedule.getLastAcademicActivityInDay(day);
			double endTime = lastActivity.getEndTime();
			ticksToEvent = TickConverter.dayTimeToTicks(day, endTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "returnHome");
		}
	}

	/**
	 * Schedule in-campus lunch
	 */
	private void scheduleLunch() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			AcademicActivity lastActivity = schedule.getLastAcademicActivityInDay(day);
			double endTime = lastActivity.getEndTime();
			if (endTime < lunchTime + lunchDuration) {
				continue;
			}
			double ticksToEvent = TickConverter.dayTimeToTicks(day, lunchTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "haveLunch");
			ticksToEvent += this.lunchDuration;
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "haveFun");
		}
	}

	/**
	 * Move to random polygon
	 * 
	 * @param polygons Array of polygons to choose
	 */
	private void moveToRandomPolygon(Object[] polygons) {
		int i = RandomHelper.nextIntFromTo(0, polygons.length - 1);
		GISPolygon selectedPolygon = (GISPolygon) polygons[i];
		moveToPolygon(selectedPolygon);
	}

	/**
	 * Move to an specific polygon
	 * 
	 * @param polygon Polygon to go to
	 */
	private void moveToPolygon(GISPolygon polygon) {
		Geometry geometry = polygon.getGeometry();
		List<Coordinate> coordinates = GeometryUtil.generateRandomPointsInPolygon(geometry, 1);
		GeometryFactory geometryFactory = new GeometryFactory();
		Coordinate coordinate = coordinates.get(0);
		Point destination = geometryFactory.createPoint(coordinate);
		this.geography.move(this, destination);
	}

}