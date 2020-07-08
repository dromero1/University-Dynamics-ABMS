package model;

import gis.GISPolygon;
import repast.simphony.space.gis.Geography;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class SupportStaff extends CommunityMember {

	/**
	 * Workplace
	 */
	private GISPolygon workplace;

	/**
	 * Work start time
	 */
	private double workStartTime;

	/**
	 * Create a new support staff agent
	 * 
	 * @param geography      Reference to geography projection
	 * @param contextBuilder Reference to the simulation builder
	 */
	public SupportStaff(Geography<Object> geography, SimulationBuilder contextBuilder) {
		super(geography, contextBuilder);
		this.workplace = Probabilities.getRandomPolygonWorkWeightBased(this.contextBuilder.workplaces);
		this.workStartTime = Probabilities.getRandomWorkStartTime();
	}

	/**
	 * Work
	 */
	public void work() {
		moveToPolygon(this.workplace, "");
	}

	/**
	 * Schedule activities
	 */
	@Override
	protected void scheduleActivities() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (int i = 1; i <= 6; i++) {
			double ticksToEvent = TickConverter.dayTimeToTicks(i, this.workStartTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "work");
		}
	}

	/**
	 * Schedule departures
	 */
	@Override
	protected void scheduleDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (int i = 1; i <= 6; i++) {
			double endTime = Probabilities.getRandomWorkEndTime();
			double ticksToEvent = TickConverter.dayTimeToTicks(i, endTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "returnHome");
		}
	}

	/**
	 * Schedule in-campus lunch
	 */
	@Override
	protected void scheduleLunch() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (int i = 1; i <= 6; i++) {
			double lunchTime = Probabilities.getRandomLunchTime();
			double lunchDuration = Probabilities.getRandomLunchDuration();
			double ticksToEvent = TickConverter.dayTimeToTicks(i, lunchTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "haveLunch");
			ticksToEvent += lunchDuration;
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "work");
		}
	}

	/**
	 * Schedule arrival planning
	 */
	@Override
	protected void scheduleArrivalPlanning() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (int i = 1; i <= 6; i++) {
			double startTime = this.workStartTime - PLANNING_DELTA;
			double ticksToEvent = TickConverter.dayTimeToTicks(i, startTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "planArrival", i);
		}
	}

}