package model;

import gis.GISPolygon;
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
	 * @param contextBuilder Reference to the simulation builder
	 */
	public SupportStaff(SimulationBuilder contextBuilder) {
		super(contextBuilder);
		this.workplace = Random.getRandomPolygonWorkWeightBased(this.contextBuilder.workplaces);
		this.workStartTime = Random.getRandomWorkStartTime();
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
			double endTime = Random.getRandomWorkEndTime();
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
			double lunchTime = Random.getRandomLunchTime();
			double lunchDuration = Random.getRandomLunchDuration();
			double ticksToEvent = TickConverter.dayTimeToTicks(i, lunchTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "haveLunch");
			ticksToEvent += lunchDuration;
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "work");
		}
	}

}