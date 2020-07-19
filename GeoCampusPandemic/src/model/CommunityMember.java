package model;

import java.util.ArrayList;
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
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.engine.schedule.ISchedulableAction;
import repast.simphony.engine.schedule.ISchedule;
import repast.simphony.parameter.Parameters;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public abstract class CommunityMember {

	/**
	 * Step interval (unit: minutes)
	 */
	public static final double STEP_INTERVAL = 15;

	/**
	 * Disease stage
	 */
	protected DiseaseStage diseaseStage;

	/**
	 * Vehicle user flag. Determines whether the student enters the campus by car or
	 * by foot.
	 */
	protected boolean isVehicleUser;

	/**
	 * Ticks to incubation end
	 */
	private double incubationShift;

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
	private ArrayList<ISchedulableAction> scheduledActions;

	/**
	 * Create a new community member agent
	 * 
	 * @param contextBuilder Reference to the simulation builder
	 * @param diseaseStage   Disease stage
	 */
	public CommunityMember(SimulationBuilder contextBuilder, DiseaseStage diseaseStage) {
		this.contextBuilder = contextBuilder;
		this.diseaseStage = diseaseStage;
		this.isVehicleUser = Random.getRandomVehicleUsage();
		this.scheduledActions = new ArrayList<>();
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
	 * Step
	 */
	public void step() {
		if (this.diseaseStage == DiseaseStage.INFECTED) {
			infect();
		}
		if (isActiveCase()) {
			this.incubationShift++;
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
	 * Set exposed
	 */
	public void setExposed() {
		this.diseaseStage = DiseaseStage.EXPOSED;
		double incubationPeriod = Random.getRandomIncubationPeriod();
		this.incubationShift = -TickConverter.daysToTicks(incubationPeriod);
		double infectiousPeriod = Math.max(incubationPeriod + Random.INFECTION_MIN, 1);
		double ticks = TickConverter.daysToTicks(infectiousPeriod);
		EventScheduler eventScheduler = EventScheduler.getInstance();
		eventScheduler.scheduleOneTimeEvent(ticks, this, "setInfected");
	}

	/**
	 * Set infected
	 */
	public void setInfected() {
		this.diseaseStage = DiseaseStage.INFECTED;
		PatientType patientType = Random.getRandomPatientType();
		String method = Random.isGoingToDie(patientType) ? "kill" : "setImmune";
		double daysToEvent = Random.getRandomTimeToDischarge() - Random.INFECTION_MIN;
		double ticks = TickConverter.daysToTicks(daysToEvent);
		EventScheduler eventScheduler = EventScheduler.getInstance();
		eventScheduler.scheduleOneTimeEvent(ticks, this, method);
	}

	/**
	 * Set immune
	 */
	public void setImmune() {
		this.diseaseStage = DiseaseStage.IMMUNE;
	}

	/**
	 * Kill community member
	 */
	public void kill() {
		this.diseaseStage = DiseaseStage.DEAD;
		removeScheduledEvents();
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
	 * Get disease stage
	 */
	public DiseaseStage getDiseaseStage() {
		return this.diseaseStage;
	}

	/**
	 * Is susceptible?
	 */
	public int isSusceptible() {
		return diseaseStage == DiseaseStage.SUSCEPTIBLE ? 1 : 0;
	}

	/**
	 * Is exposed?
	 */
	public int isExposed() {
		return diseaseStage == DiseaseStage.EXPOSED ? 1 : 0;
	}

	/**
	 * Is infected?
	 */
	public int isInfected() {
		return diseaseStage == DiseaseStage.INFECTED ? 1 : 0;
	}

	/**
	 * Is immune?
	 */
	public int isImmune() {
		return diseaseStage == DiseaseStage.IMMUNE ? 1 : 0;
	}

	/**
	 * Is dead?
	 */
	public int isDead() {
		return diseaseStage == DiseaseStage.DEAD ? 1 : 0;
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
	 * Schedule step
	 */
	protected void scheduleStep() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		double tickInterval = TickConverter.minutesToTicks(STEP_INTERVAL);
		ISchedulableAction stepAction = eventScheduler.scheduleRecurringEvent(1, this, tickInterval, "step");
		this.scheduledActions.add(stepAction);
	}

	/**
	 * Get random polygon
	 * 
	 * @param polygons          Map of polygons to choose from
	 * @param selectionStrategy Selection strategy
	 */
	protected GISPolygon getRandomPolygon(HashMap<String, GISPolygon> polygons, SelectionStrategy strategy) {
		GISPolygon selectedPolygon = null;
		switch (strategy) {
		case WEIGHT_BASED:
			selectedPolygon = Random.getRandomPolygonWeightBased(polygons);
			break;
		default:
			selectedPolygon = Random.getRandomPolygon(polygons);
			break;
		}
		return selectedPolygon;
	}

	/**
	 * Move to an specific polygon. Find the shortest route and traverse the route
	 * graph.
	 * 
	 * @param polygon Polygon to go to
	 * @param method  Method to call after arriving to polygon
	 */
	protected void moveToPolygon(GISPolygon polygon, String method) {
		String source = this.currentPolygon.getId();
		String sink = polygon.getId();
		Graph<String, DefaultWeightedEdge> routes = this.contextBuilder.routes;
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths = this.contextBuilder.shortestPaths;
		GraphPath<String, DefaultWeightedEdge> path = shortestPaths.get(source + "-" + sink);
		List<String> vertexes = path.getVertexList();
		List<DefaultWeightedEdge> edges = path.getEdgeList();
		EventScheduler eventScheduler = EventScheduler.getInstance();
		double speed = Random.getRandomWalkingSpeed();
		double totalTime = 0.0;
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
		switch (this.diseaseStage) {
		case EXPOSED:
			setExposed();
			break;
		case INFECTED:
			setInfected();
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
		scheduleStep();
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
		for (Student student : students) {
			if (student.diseaseStage == DiseaseStage.SUSCEPTIBLE && Random.isGettingExposed(incubationShift)) {
				student.setExposed();
			}
		}
		Iterable<Staffer> staffers = this.contextBuilder.geography.getObjectsWithin(searchEnvelope, Staffer.class);
		for (Staffer staffer : staffers) {
			if (staffer.diseaseStage == DiseaseStage.SUSCEPTIBLE && Random.isGettingExposed(incubationShift)) {
				staffer.setExposed();
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

	/**
	 * Is an active case (infected or exposed)?
	 */
	private boolean isActiveCase() {
		return this.diseaseStage == DiseaseStage.INFECTED || this.diseaseStage == DiseaseStage.EXPOSED;
	}

	/**
	 * Remove scheduled events
	 */
	private void removeScheduledEvents() {
		ISchedule schedule = RunEnvironment.getInstance().getCurrentSchedule();
		for (int i = 0; i < this.scheduledActions.size(); i++) {
			ISchedulableAction action = this.scheduledActions.get(i);
			schedule.removeAction(action);
			this.scheduledActions.remove(action);
		}
	}

}