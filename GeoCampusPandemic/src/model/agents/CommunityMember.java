package model.agents;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;
import config.SourceFeatures;
import gis.GISDensityMeter;
import gis.GISPolygon;
import model.disease.Compartment;
import model.disease.PatientType;
import model.learning.LearningFactory;
import model.learning.LearningMechanism;
import model.learning.LearningStyle;
import model.learning.SelectionStrategy;
import model.util.Randomizer;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.gis.util.GeometryUtil;
import simulation.EventScheduler;
import simulation.ParametersAdapter;
import simulation.SimulationBuilder;
import util.PolygonUtil;
import util.TickConverter;

public abstract class CommunityMember {

	/**
	 * Upper bound of arrival shift (unit: hours)
	 */
	protected static final double UB_ARRIVAL_SHIFT = 1.0;

	/**
	 * Compartment
	 */
	protected Compartment compartment;

	/**
	 * Time to outbreak (unit: hours)
	 */
	protected double outbreakTick;

	/**
	 * Time to incubation end (unit: hours)
	 */
	protected double incubationEnd;

	/**
	 * Learning mechanism
	 */
	protected LearningMechanism learningMechanism;

	/**
	 * Vehicle user flag. Determines whether the student enters the campus by
	 * car or by foot.
	 */
	protected boolean isVehicleUser;

	/**
	 * Current polygon
	 */
	protected GISPolygon currentPolygon;

	/**
	 * Last exit
	 */
	protected GISPolygon lastExit;

	/**
	 * Reference to simulation builder
	 */
	protected SimulationBuilder simulationBuilder;

	/**
	 * Scheduled actions
	 */
	protected Map<SchedulableAction, List<ISchedulableAction>> scheduledActions;

	/**
	 * Create a new community member agent
	 * 
	 * @param simulationBuilder Reference to the simulation builder
	 * @param compartment       Compartment
	 * @param outbreakTick      Outbreak tick
	 */
	public CommunityMember(SimulationBuilder simulationBuilder,
			Compartment compartment, double outbreakTick) {
		this.simulationBuilder = simulationBuilder;
		this.compartment = compartment;
		this.outbreakTick = outbreakTick;
		this.isVehicleUser = Randomizer.getRandomVehicleUsage();
		this.scheduledActions = new EnumMap<>(SchedulableAction.class);
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0)
	public void init() {
		initDisease();
		initLearning();
		returnHome();
		scheduleRecurringEvents();
	}

	/**
	 * Expel particles
	 */
	public void expelParticles() {
		if (isInCampus()) {
			infect();
		}
	}

	/**
	 * Return to homeplace. Move to a random in-out spot and vanish to limbo.
	 */
	public void returnHome() {
		this.lastExit = getRandomInOutSpot();
		if (this.currentPolygon != null) {
			moveToPolygon(this.lastExit, "vanishToLimbo");
		} else {
			vanishToLimbo();
		}
	}

	/**
	 * Go have lunch at a designated eating place
	 */
	public void haveLunch() {
		SelectionStrategy selectionStrategy = ParametersAdapter
				.getSelectionStrategy();
		GISPolygon polygon = getRandomPolygon(
				this.simulationBuilder.eatingPlaces, selectionStrategy);
		moveToPolygon(polygon, "");
	}

	/**
	 * Transition to the exposed compartment
	 */
	public void transitionToExposed() {
		this.compartment = Compartment.EXPOSED;
		double incubationPeriod = Randomizer.getRandomIncubationPeriod();
		double infectiousPeriod = Math
				.max(incubationPeriod + Randomizer.INFECTION_MIN, 1);
		this.incubationEnd = RepastEssentials.GetTickCount()
				+ TickConverter.daysToTicks(incubationPeriod);
		double ticks = TickConverter.daysToTicks(infectiousPeriod);
		EventScheduler eventScheduler = EventScheduler.getInstance();
		eventScheduler.scheduleOneTimeEvent(ticks, this,
				"transitionToInfected");
	}

	/**
	 * Transition to the infected compartment
	 */
	public void transitionToInfected() {
		this.compartment = Compartment.INFECTED;
		PatientType patientType = Randomizer.getRandomPatientType();
		// Schedule regular particle expulsion
		EventScheduler eventScheduler = EventScheduler.getInstance();
		double expulsionInterval = ParametersAdapter
				.getParticleExpulsionInterval();
		double expelInterval = TickConverter.minutesToTicks(expulsionInterval);
		ISchedulableAction expelAction = eventScheduler.scheduleRecurringEvent(
				1, this, expelInterval, "expelParticles");
		List<ISchedulableAction> actions = new ArrayList<>();
		actions.add(expelAction);
		this.scheduledActions.put(SchedulableAction.EXPEL_PARTICLES, actions);
		// Schedule removal
		boolean isDying = Randomizer.isGoingToDie(patientType);
		String removalMethod = (isDying) ? "die" : "transitionToImmune";
		double timeToDischarge = Randomizer.getRandomTimeToDischarge();
		double ticksToRemoval = TickConverter
				.daysToTicks(timeToDischarge - Randomizer.INFECTION_MIN);
		eventScheduler.scheduleOneTimeEvent(ticksToRemoval, this,
				removalMethod);
	}

	/**
	 * Transition to the immune compartment
	 */
	public void transitionToImmune() {
		this.compartment = Compartment.IMMUNE;
		unscheduleAction(SchedulableAction.EXPEL_PARTICLES);
	}

	/**
	 * Transition to the dead compartment
	 */
	public void die() {
		this.compartment = Compartment.DEAD;
		unscheduleAction(SchedulableAction.ATTEND_ACTIVITY);
		unscheduleAction(SchedulableAction.ARRIVE_CAMPUS);
		unscheduleAction(SchedulableAction.RETURN_HOME);
		unscheduleAction(SchedulableAction.HAVE_LUNCH);
		unscheduleAction(SchedulableAction.EXPEL_PARTICLES);
	}

	/**
	 * Vanish to a limbo. A limbo emulates what's off campus.
	 */
	public void vanishToLimbo() {
		String limboId = this.lastExit.getLink();
		GISPolygon limbo = this.simulationBuilder.getPolygonById(limboId);
		if (this.currentPolygon == null) {
			this.currentPolygon = limbo;
		}
		relocate(limbo);
	}

	/**
	 * Relocate to an specific polygon
	 * 
	 * @param polygon Polygon to go to
	 */
	public void relocate(GISPolygon polygon) {
		// Relocation
		Point destination = PolygonUtil.getRandomPoint(polygon);
		this.simulationBuilder.geography.move(this, destination);
		this.currentPolygon.onDeparture();
		this.currentPolygon = polygon;
		this.currentPolygon.onArrival();
		// Update learning
		String currentLocation = polygon.getId();
		if (this.learningMechanism.containsState(currentLocation)) {
			GISDensityMeter densityMeter = (GISDensityMeter) this.currentPolygon;
			double socialDistancing = ParametersAdapter.getSocialDistancing();
			double density = densityMeter.measureDensity();
			double reward = (1.0 / socialDistancing) - density;
			this.learningMechanism.updateLearning(currentLocation, reward);
		}
	}

	/**
	 * Get compartment
	 */
	public Compartment getCompartment() {
		return this.compartment;
	}

	/**
	 * Is in campus?
	 */
	public boolean isInCampus() {
		String polygonId = this.currentPolygon.getId();
		return !this.simulationBuilder.limbos.containsKey(polygonId);
	}

	/**
	 * Is susceptible?
	 */
	public int isSusceptible() {
		return this.compartment == Compartment.SUSCEPTIBLE ? 1 : 0;
	}

	/**
	 * Is exposed?
	 */
	public int isExposed() {
		return this.compartment == Compartment.EXPOSED ? 1 : 0;
	}

	/**
	 * Is infected?
	 */
	public int isInfected() {
		return this.compartment == Compartment.INFECTED ? 1 : 0;
	}

	/**
	 * Is immune?
	 */
	public int isImmune() {
		return this.compartment == Compartment.IMMUNE ? 1 : 0;
	}

	/**
	 * Is dead?
	 */
	public int isDead() {
		return this.compartment == Compartment.DEAD ? 1 : 0;
	}

	/**
	 * Is an active case?
	 */
	public int isActiveCase() {
		return this.compartment == Compartment.EXPOSED
				|| this.compartment == Compartment.INFECTED ? 1 : 0;
	}

	/**
	 * Schedule activities
	 */
	protected abstract void scheduleActivities();

	/**
	 * Schedule arrivals
	 */
	protected abstract void scheduleArrivals();

	/**
	 * Schedule departures
	 */
	protected abstract void scheduleDepartures();

	/**
	 * Schedule in-campus lunch
	 */
	protected abstract void scheduleLunch();

	/**
	 * Get random polygon
	 * 
	 * @param polygons Map of polygons to choose from
	 * @param strategy Selection strategy
	 */
	protected GISPolygon getRandomPolygon(Map<String, GISPolygon> polygons,
			SelectionStrategy strategy) {
		GISPolygon selectedPolygon = null;
		switch (strategy) {
		case RL_BASED:
			String currentLocation = this.currentPolygon.getId();
			if (this.learningMechanism.containsState(currentLocation)) {
				String destination = this.learningMechanism
						.selectAction(currentLocation);
				selectedPolygon = this.simulationBuilder
						.getPolygonById(destination);
			} else {
				selectedPolygon = getRandomPolygon(polygons,
						SelectionStrategy.RANDOM);
			}
			break;
		case WEIGHT_BASED:
			selectedPolygon = Randomizer.getRandomPolygonWeightBased(polygons);
			break;
		default:
			selectedPolygon = Randomizer.getRandomPolygon(polygons);
			break;
		}
		return selectedPolygon;

	}

	/**
	 * Move to an specific polygon. Find the shortest route and traverse the
	 * graph.
	 * 
	 * @param polygon Polygon to go to
	 * @param method  Method to call after arriving to polygon
	 */
	protected void moveToPolygon(GISPolygon polygon, String method) {
		String source = this.currentPolygon.getId();
		String sink = polygon.getId();
		// Get shortest paths
		Graph<String, DefaultWeightedEdge> routes = this.simulationBuilder.routes;
		Map<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths = this.simulationBuilder.shortestPaths;
		String pathId = source + SourceFeatures.ENTITY_SEPARATOR + sink;
		GraphPath<String, DefaultWeightedEdge> path = shortestPaths.get(pathId);
		List<String> vertexes = path.getVertexList();
		List<DefaultWeightedEdge> edges = path.getEdgeList();
		// Schedule relocations
		EventScheduler eventScheduler = EventScheduler.getInstance();
		double totalTime = 0.0;
		double speed = Randomizer.getRandomWalkingSpeed();
		for (int i = 0; i < vertexes.size() - 1; i++) {
			String id = vertexes.get(i + 1);
			GISPolygon nextPolygon = this.simulationBuilder.getPolygonById(id);
			DefaultWeightedEdge edge = edges.get(i);
			double meters = routes.getEdgeWeight(edge);
			double minutes = meters / speed;
			totalTime += minutes;
			double ticks = TickConverter.minutesToTicks(totalTime);
			eventScheduler.scheduleOneTimeEvent(ticks, this, "relocate",
					nextPolygon);
		}
		// Schedule method
		if (!method.isEmpty()) {
			totalTime += 1;
			double ticks = TickConverter.minutesToTicks(totalTime);
			eventScheduler.scheduleOneTimeEvent(ticks, this, method);
		}
	}

	/**
	 * Initialize disease
	 */
	private void initDisease() {
		if (this.compartment == Compartment.EXPOSED) {
			this.compartment = Compartment.SUSCEPTIBLE;
			EventScheduler eventScheduler = EventScheduler.getInstance();
			eventScheduler.scheduleOneTimeEvent(this.outbreakTick, this,
					"transitionToExposed");
		}
	}

	/**
	 * Initialize learning
	 */
	private void initLearning() {
		LearningStyle learningStyle = ParametersAdapter.getLearningStyle();
		this.learningMechanism = LearningFactory.makeLearningMechanism(
				learningStyle, this.simulationBuilder.teachingFacilities,
				this.simulationBuilder.sharedAreas,
				this.simulationBuilder.eatingPlaces);
	}

	/**
	 * Schedule recurring events
	 */
	private void scheduleRecurringEvents() {
		scheduleActivities();
		scheduleArrivals();
		scheduleDepartures();
		scheduleLunch();
	}

	/**
	 * Infect nearby susceptible individuals
	 */
	private void infect() {
		double distance = ParametersAdapter.getInfectionRadius();
		Geometry searchArea = GeometryUtil.generateBuffer(
				this.simulationBuilder.geography,
				this.simulationBuilder.geography.getGeometry(this), distance);
		Envelope searchEnvelope = searchArea.getEnvelopeInternal();
		Iterable<Student> students = this.simulationBuilder.geography
				.getObjectsWithin(searchEnvelope, Student.class);
		double incubationDiff = RepastEssentials.GetTickCount()
				- this.incubationEnd;
		for (Student student : students) {
			if (student.compartment == Compartment.SUSCEPTIBLE
					&& Randomizer.isGettingExposed(incubationDiff)) {
				student.transitionToExposed();
				student.currentPolygon.onEffectiveContact();
			}
		}
		Iterable<Staffer> staffers = this.simulationBuilder.geography
				.getObjectsWithin(searchEnvelope, Staffer.class);
		for (Staffer staffer : staffers) {
			if (staffer.compartment == Compartment.SUSCEPTIBLE
					&& Randomizer.isGettingExposed(incubationDiff)) {
				staffer.transitionToExposed();
				staffer.currentPolygon.onEffectiveContact();
			}
		}
	}

	/**
	 * Get random in-out spot
	 */
	private GISPolygon getRandomInOutSpot() {
		Map<String, GISPolygon> inOuts = null;
		if (this.isVehicleUser) {
			inOuts = this.simulationBuilder.vehicleInOuts;
		} else {
			inOuts = this.simulationBuilder.inOuts;
		}
		return getRandomPolygon(inOuts, SelectionStrategy.RANDOM);
	}

	/**
	 * Unschedule action
	 * 
	 * @param schedulableAction Action to unschedule
	 */
	private void unscheduleAction(SchedulableAction schedulableAction) {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		for (ISchedulableAction action : this.scheduledActions
				.get(schedulableAction)) {
			schedule.removeAction(action);
		}
		this.scheduledActions.remove(schedulableAction);
	}

}