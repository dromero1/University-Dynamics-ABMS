package model.agents;

import java.util.ArrayList;
import java.util.List;

import gis.GISPolygon;
import model.disease.Compartment;
import model.util.Randomizer;
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
		this.workStartTime = Randomizer.getRandomStafferArrivalTime();
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
		List<ISchedulableAction> actions = new ArrayList<>();
		for (int i = 1; i <= WEEKDAYS; i++) {
			double ticksToEvent = TickConverter.dayTimeToTicks(i, this.workStartTime);
			ISchedulableAction workAction = eventScheduler.scheduleRecurringEvent(ticksToEvent, this,
					TickConverter.TICKS_PER_WEEK, "work");
			actions.add(workAction);
		}
		this.scheduledActions.put(SchedulableAction.ATTEND_ACTIVITY, actions);
	}

	/**
	 * Schedule arrivals
	 */
	@Override
	protected void scheduleArrivals() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		List<ISchedulableAction> actions = new ArrayList<>();
		for (int i = 1; i <= WEEKDAYS; i++) {
			double arrivalTime = Randomizer.getRandomStafferArrivalTime();
			double startTime = Math.min(arrivalTime, this.workStartTime - 1);
			double ticksToEvent = TickConverter.dayTimeToTicks(i, startTime);
			ISchedulableAction arriveCampusAction = eventScheduler.scheduleRecurringEvent(ticksToEvent, this,
					TickConverter.TICKS_PER_WEEK, "haveLunch");
			actions.add(arriveCampusAction);
		}
		this.scheduledActions.put(SchedulableAction.ARRIVE_CAMPUS, actions);
	}

	/**
	 * Schedule departures
	 */
	@Override
	protected void scheduleDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		List<ISchedulableAction> actions = new ArrayList<>();
		for (int i = 1; i <= WEEKDAYS; i++) {
			double endTime = Randomizer.getRandomStafferDepartureTime();
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
		List<ISchedulableAction> actions = new ArrayList<>();
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