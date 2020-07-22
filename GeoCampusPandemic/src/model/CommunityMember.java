package model;

import java.util.HashMap;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import gis.GISPolygon;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.parameter.Parameters;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public abstract class CommunityMember {

	/**
	 * Expulsion interval (unit: minutes)
	 */
	public static final double EXPULSION_INTERVAL = 15;

	/**
	 * Compartment
	 */
	protected Compartment compartment;

	/**
	 * Incubation end (unit: hour)
	 */
	protected double incubationEnd;

	/**
	 * Vehicle user flag. Determines whether the student enters the campus by car or
	 * by foot.
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
	protected SimulationBuilder contextBuilder;

	/**
	 * Scheduled actions
	 */
	private HashMap<SchedulableAction, ISchedulableAction> schedulableActions;

	/**
	 * Create a new community member agent
	 * 
	 * @param contextBuilder Reference to the simulation builder
	 * @param compartment    Compartment
	 */
	public CommunityMember(SimulationBuilder contextBuilder, Compartment compartment) {
		this.contextBuilder = contextBuilder;
		this.compartment = compartment;
		this.isVehicleUser = Probabilities.getRandomVehicleUsage();
		this.schedulableActions = new HashMap<>();
	}

	/**
	 * Initialize
	 */
	@ScheduledMethod(start = 0)
	public void init() {
		goHome();
		initDisease();
		scheduleRecurringEvents();
	}

	/**
	 * Expel particles
	 */
	public void expel() {
		if (isInCampus()) {
			infect();
		}
	}

	/**
	 * Go home. Move to a random in-out spot and vanish to limbo.
	 */
	public void goHome() {
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
		GISPolygon polygon = getRandomPolygon(this.contextBuilder.eatingPlaces, SelectionStrategy.WEIGHT_BASED);
		moveToPolygon(polygon, "");
	}

	/**
	 * Transition to the exposed compartment
	 */
	public void transitionToExposed() {
		this.compartment = Compartment.EXPOSED;
		double incubationPeriod = Probabilities.getRandomIncubationPeriod();
		double infectiousPeriod = Math.max(incubationPeriod + Probabilities.INFECTION_MIN, 1);
		this.incubationEnd = RepastEssentials.GetTickCount() + TickConverter.daysToTicks(incubationPeriod);
		double ticks = TickConverter.daysToTicks(infectiousPeriod);
		EventScheduler eventScheduler = EventScheduler.getInstance();
		eventScheduler.scheduleOneTimeEvent(ticks, this, "transitionToInfected");
	}

	/**
	 * Transition to the infected compartment
	 */
	public void transitionToInfected() {
		this.compartment = Compartment.INFECTED;
		PatientType patientType = Probabilities.getRandomPatientType();
		// Schedule regular expulsion
		EventScheduler eventScheduler = EventScheduler.getInstance();
		double expelInterval = TickConverter.minutesToTicks(EXPULSION_INTERVAL);
		ISchedulableAction expelAction = eventScheduler.scheduleRecurringEvent(1, this, expelInterval, "expel");
		this.schedulableActions.put(SchedulableAction.EXPEL, expelAction);
		// Schedule removal
		boolean isDying = Probabilities.isGoingToDie(patientType);
		String removalMethod = (isDying) ? "die" : "transitionToImmune";
		double timeToDischarge = Probabilities.getRandomTimeToDischarge();
		double ticksToRemoval = TickConverter.daysToTicks(timeToDischarge - Probabilities.INFECTION_MIN);
		eventScheduler.scheduleOneTimeEvent(ticksToRemoval, this, removalMethod);
	}

	/**
	 * Transition to the immune compartment
	 */
	public void transitionToImmune() {
		this.compartment = Compartment.IMMUNE;
		this.schedulableActions.remove(SchedulableAction.EXPEL);
	}

	/**
	 * Transition to the dead compartment
	 */
	public void die() {
		this.compartment = Compartment.DEAD;
		this.schedulableActions.remove(SchedulableAction.EXPEL);
	}

	/**
	 * Vanish to limbo. A limbo emulates what's off campus.
	 */
	public void vanishToLimbo() {
		String limboId = this.lastExit.getLink();
		GISPolygon limbo = this.contextBuilder.getPolygonById(limboId);
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
		Geometry geometry = polygon.getGeometry();
		List<Coordinate> coordinates = GeometryUtil.generateRandomPointsInPolygon(geometry, 1);
		GeometryFactory geometryFactory = new GeometryFactory();
		Coordinate coordinate = coordinates.get(0);
		Point destination = geometryFactory.createPoint(coordinate);
		this.contextBuilder.geography.move(this, destination);
		this.currentPolygon.onDeparture();
		this.currentPolygon = polygon;
		this.currentPolygon.onArrival();
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
		return !this.contextBuilder.limbos.containsKey(polygonId);
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
	 * Is active case?
	 */
	public int isActiveCase() {
		return this.compartment == Compartment.EXPOSED || this.compartment == Compartment.INFECTED ? 1 : 0;
	}

	/**
	 * Schedule activities
	 */
	protected abstract void scheduleActivities();

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
	protected GISPolygon getRandomPolygon(HashMap<String, GISPolygon> polygons, SelectionStrategy strategy) {
		GISPolygon selectedPolygon = null;
		switch (strategy) {
		case WEIGHT_BASED:
			selectedPolygon = Probabilities.getRandomPolygonWeightBased(polygons);
			break;
		default:
			selectedPolygon = Probabilities.getRandomPolygon(polygons);
			break;
		}
		return selectedPolygon;
	}

	/**
	 * Move to an specific polygon. Find the shortest route and traverse the graph.
	 * 
	 * @param polygon Polygon to go to
	 * @param method  Method to call after arriving to polygon
	 */
	protected void moveToPolygon(GISPolygon polygon, String method) {
		String source = this.currentPolygon.getId();
		String sink = polygon.getId();
		// Get shortest paths
		Graph<String, DefaultWeightedEdge> routes = this.contextBuilder.routes;
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths = this.contextBuilder.shortestPaths;
		GraphPath<String, DefaultWeightedEdge> path = shortestPaths.get(source + "-" + sink);
		List<String> vertexes = path.getVertexList();
		List<DefaultWeightedEdge> edges = path.getEdgeList();
		// Schedule relocations
		EventScheduler eventScheduler = EventScheduler.getInstance();
		double totalTime = 0.0;
		double speed = Probabilities.getRandomWalkingSpeed();
		for (int i = 0; i < vertexes.size() - 1; i++) {
			String id = vertexes.get(i + 1);
			GISPolygon nextPolygon = this.contextBuilder.getPolygonById(id);
			DefaultWeightedEdge edge = edges.get(i);
			double meters = routes.getEdgeWeight(edge);
			double minutes = meters / speed;
			totalTime += minutes;
			double ticks = TickConverter.minutesToTicks(totalTime);
			eventScheduler.scheduleOneTimeEvent(ticks, this, "relocate", nextPolygon);
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
		switch (this.compartment) {
		case EXPOSED:
			transitionToExposed();
			break;
		case INFECTED:
			this.incubationEnd = -TickConverter.daysToTicks(Probabilities.INFECTION_MIN);
			transitionToInfected();
			break;
		default:
			break;
		}
	}

	/**
	 * Schedule recurring events
	 */
	private void scheduleRecurringEvents() {
		scheduleActivities();
		scheduleDepartures();
		scheduleLunch();
	}

	/**
	 * Infect nearby susceptible individuals
	 */
	private void infect() {
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		double distance = simParams.getDouble("infectionRadius");
		Geometry searchArea = GeometryUtil.generateBuffer(this.contextBuilder.geography,
				this.contextBuilder.geography.getGeometry(this), distance);
		Envelope searchEnvelope = searchArea.getEnvelopeInternal();
		Iterable<Student> students = this.contextBuilder.geography.getObjectsWithin(searchEnvelope, Student.class);
		double incubationDiff = RepastEssentials.GetTickCount() - this.incubationEnd;
		for (Student student : students) {
			if (student.compartment == Compartment.SUSCEPTIBLE && Probabilities.isGettingExposed(incubationDiff)) {
				student.transitionToExposed();
				student.currentPolygon.onEffectiveContact();
			}
		}
		Iterable<Staffer> staffers = this.contextBuilder.geography.getObjectsWithin(searchEnvelope, Staffer.class);
		for (Staffer staffer : staffers) {
			if (staffer.compartment == Compartment.SUSCEPTIBLE && Probabilities.isGettingExposed(incubationDiff)) {
				staffer.transitionToExposed();
				staffer.currentPolygon.onEffectiveContact();
			}
		}
	}

	/**
	 * Get random in-out spot
	 */
	private GISPolygon getRandomInOutSpot() {
		HashMap<String, GISPolygon> inOuts = null;
		if (this.isVehicleUser) {
			inOuts = this.contextBuilder.vehicleInOuts;
		} else {
			inOuts = this.contextBuilder.inOuts;
		}
		return getRandomPolygon(inOuts, SelectionStrategy.RANDOM);
	}

}