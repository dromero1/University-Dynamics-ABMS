package model;

import java.util.ArrayList;
import java.util.HashMap;
import gis.GISPolygon;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.Pair;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class Student extends CommunityMember {

	/**
	 * Minimum time difference between activities in order to have fun (unit: hours)
	 */
	public static final double MIN_TIME_TO_FUN = 0.5;

	/**
	 * Student id
	 */
	private String id;

	/**
	 * Academic schedule
	 */
	private Schedule schedule;

	/**
	 * Create a new student agent
	 * 
	 * @param geography      Reference to geography projection
	 * @param contextBuilder Reference to the simulation builder
	 * @param id             Student id
	 */
	public Student(Geography<Object> geography, SimulationBuilder contextBuilder, String id) {
		super(geography, contextBuilder);
		this.id = id;
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
	 * Attend an academic activity at a teaching facility
	 * 
	 * @param teachingFacilityId Id of the teaching facility
	 */
	public void attendActivity(String teachingFacilityId) {
		HashMap<String, GISPolygon> teachingFacilities = this.contextBuilder.teachingFacilities;
		GISPolygon teachingFacility = teachingFacilities.get(teachingFacilityId);
		moveToPolygon(teachingFacility, "");
	}

	/**
	 * Leave an academic activity. The student determines what to do next. If he/she
	 * has an activity in less than MIN_TIME_TO_FUN ticks he/she prefers to go
	 * there. In the other case, the student goes to have fun.
	 */
	public void leaveActivity() {
		double tick = RepastEssentials.GetTickCount();
		Pair<Double, Double> dayTime = TickConverter.tickToDayTime(tick);
		double day = dayTime.getFirst();
		double hour = dayTime.getSecond();
		AcademicActivity nextActivity = this.schedule.getNextAcademicActivity(day, hour);
		if (nextActivity != null) {
			double delta = nextActivity.getStartTime() - hour;
			if (delta < MIN_TIME_TO_FUN) {
				attendActivity(nextActivity.getTeachingFacilityId());
			} else {
				haveFun();
			}
		}
	}

	/**
	 * Go have fun at a shared area
	 */
	public void haveFun() {
		moveToRandomPolygon(this.contextBuilder.sharedAreas, "", SelectionStrategy.WEIGHT_BASED);
	}

	/**
	 * Get student id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Schedule activities
	 */
	@Override
	protected void scheduleActivities() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (Group group : this.schedule.getGroups()) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				int day = activity.getDay();
				double startTime = activity.getStartTime();
				String teachingFacilityId = activity.getTeachingFacilityId();
				double ticksToEvent = TickConverter.dayTimeToTicks(day, startTime);
				eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
						"attendActivity", teachingFacilityId);
				double endTime = activity.getEndTime();
				ticksToEvent = TickConverter.dayTimeToTicks(day, endTime);
				eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
						"leaveActivity");
			}
		}
	}

	/**
	 * Schedule departures
	 */
	@Override
	protected void scheduleDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			AcademicActivity lastActivity = this.schedule.getLastAcademicActivityInDay(day);
			double endTime = lastActivity.getEndTime();
			double ticksToEvent = TickConverter.dayTimeToTicks(day, endTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "returnHome");
		}
	}

	/**
	 * Schedule in-campus lunch
	 */
	@Override
	protected void scheduleLunch() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			Pair<Double, Double> lunch = Heuristics.getRandomLunchTime(this.schedule, day);
			if (lunch == null)
				continue;
			double lunchTime = lunch.getFirst();
			double lunchDuration = lunch.getSecond();
			double ticksToEvent = TickConverter.dayTimeToTicks(day, lunchTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "haveLunch");
			ticksToEvent += lunchDuration;
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "haveFun");
		}
	}

	/**
	 * Schedule arrival planning
	 */
	@Override
	protected void scheduleArrivalPlanning() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			AcademicActivity firstActivity = this.schedule.getFirstAcademicActivityInDay(day);
			double startTime = firstActivity.getStartTime() - PLANNING_DELTA;
			double ticksToEvent = TickConverter.dayTimeToTicks(day, startTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "planArrival", day);
		}
	}

}