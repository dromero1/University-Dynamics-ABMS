package model;

import java.util.ArrayList;
import gis.GISPolygon;
import repast.simphony.engine.schedule.ISchedulableAction;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class Staffer extends CommunityMember {

	/**
	 * Week days
	 */
	public static final int WEEKDAYS = 5;

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
		this.workplace = Randomizer.getRandomPolygonWorkWeightBased(this.simulationBuilder.workplaces);
		this.workStartTime = Randomizer.getRandomWorkStartTime();
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
		ArrayList<ISchedulableAction> actions = new ArrayList<>();
		for (int i = 1; i <= WEEKDAYS; i++) {
			double ticksToEvent = TickConverter.dayTimeToTicks(i, this.workStartTime);
			ISchedulableAction workAction = eventScheduler.scheduleRecurringEvent(ticksToEvent, this,
					TickConverter.TICKS_PER_WEEK, "work");
			actions.add(workAction);
		}
		this.scheduledActions.put(SchedulableAction.ATTEND_ACTIVITY, actions);
	}

	/**
	 * Schedule departures
	 */
	@Override
	protected void scheduleDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<ISchedulableAction> actions = new ArrayList<>();
		for (int i = 1; i <= WEEKDAYS; i++) {
			double endTime = Randomizer.getRandomWorkEndTime();
			double ticksToEvent = TickConverter.dayTimeToTicks(i, endTime);
			ISchedulableAction returnHomeAction = eventScheduler.scheduleRecurringEvent(ticksToEvent, this,
					TickConverter.TICKS_PER_WEEK, "returnHome");
			actions.add(returnHomeAction);
		}
		this.scheduledActions.put(SchedulableAction.RETURN_HOME, actions);
	}

	/**
	 * Schedule in-campus lunch
	 */
	@Override
	protected void scheduleLunch() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<ISchedulableAction> actions = new ArrayList<>();
		for (int i = 1; i <= WEEKDAYS; i++) {
			double lunchTime = Randomizer.getRandomLunchTime();
			double lunchDuration = Randomizer.getRandomLunchDuration();
			double ticksToEvent = TickConverter.dayTimeToTicks(i, lunchTime);
			ISchedulableAction haveLunchAction = eventScheduler.scheduleRecurringEvent(ticksToEvent, this,
					TickConverter.TICKS_PER_WEEK, "haveLunch");
			actions.add(haveLunchAction);
			ticksToEvent += lunchDuration;
			ISchedulableAction workAction = eventScheduler.scheduleRecurringEvent(ticksToEvent, this,
					TickConverter.TICKS_PER_WEEK, "work");
			actions.add(workAction);
		}
		this.scheduledActions.put(SchedulableAction.HAVE_LUNCH, actions);
	}

}