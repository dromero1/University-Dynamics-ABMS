package model;

import gis.GISPolygon;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class Staffer extends CommunityMember {

	/**
	 * Workplace
	 */
	private GISPolygon workplace;

	/**
	 * Work start time
	 */
	private double workStartTime;

	/**
	 * Create a new staffer agent
	 * 
	 * @param contextBuilder Reference to the simulation builder
	 * @param compartment    Compartment
	 */
	public Staffer(SimulationBuilder contextBuilder, Compartment compartment) {
		super(contextBuilder, compartment);
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
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK, "goHome");
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