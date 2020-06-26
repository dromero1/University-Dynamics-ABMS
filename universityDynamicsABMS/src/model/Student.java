package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import gis.GISDensityMeter;
import gis.GISLimbo;
import gis.GISPolygon;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.Pair;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public class Student {

	/**
	 * Arrival delta
	 */
	private static final double ARRIVAL_DELTA = 1.0 / 6;

	/**
	 * Academic schedule
	 */
	private Schedule schedule;

	/**
	 * Learning flag. Determines whether the student is currently learning or not.
	 */
	private boolean learning;

	/**
	 * Vehicle user flag. Determines whether the student enters the campus by car or
	 * by foot.
	 */
	private boolean isVehicleUser;

	/**
	 * Reference to geography projection
	 */
	private Geography<Object> geography;

	/**
	 * Reference to simulation builder
	 */
	private SimulationBuilder contextBuilder;

	/**
	 * Current polygon
	 */
	private GISPolygon currentPolygon;

	/**
	 * Action's values estimates
	 */
	private HashMap<String, Pair<Double, Integer>> actionValueEstimates;

	/**
	 * Last reward
	 */
	private double lastReward;

	/**
	 * Create a new student agent
	 * 
	 * @param geography      Reference to geography projection
	 * @param contextBuilder Reference to the simulation builder
	 */
	public Student(Geography<Object> geography, SimulationBuilder contextBuilder, boolean isVehicleUser) {
		this.geography = geography;
		this.contextBuilder = contextBuilder;
		this.learning = false;
		this.isVehicleUser = isVehicleUser;
		initValueEstimations();
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
	 * Attend an academic activity at a teaching facility
	 * 
	 * @param teachingFacilityId Id of the teaching facility
	 */
	public void attendActivity(String teachingFacilityId) {
		HashMap<String, GISPolygon> teachingFacilities = this.contextBuilder.getTeachingFacilities();
		GISPolygon teachingFacility = teachingFacilities.get(teachingFacilityId);
		moveToPolygon(teachingFacility, "activateLearningMode");
	}

	/**
	 * Leave an academic activity. The student determines what to do next. If he/she
	 * has an activity in less than ARRIVAL_DELTA ticks he/she prefers to go there.
	 * In the other case, the student goes to have fun.
	 */
	public void leaveActivity() {
		deactivateLearningMode();
		double tick = RepastEssentials.GetTickCount();
		Pair<Double, Double> dayTime = TickConverter.tickToDayTime(tick);
		double day = dayTime.getFirst();
		double hour = dayTime.getSecond();
		AcademicActivity nextActivity = this.schedule.getNextAcademicActivity(day, hour);
		if (nextActivity != null) {
			double delta = nextActivity.getStartTime() - hour;
			if (delta < ARRIVAL_DELTA) {
				attendActivity(nextActivity.getTeachingFacilityId());
			} else {
				EventScheduler eventScheduler = EventScheduler.getInstance();
				eventScheduler.scheduleOneTimeEvent(1, this, "haveFun");
			}
		}
	}

	/**
	 * Go have lunch at a designated eating place
	 */
	public void haveLunch() {
		moveToRandomPolygon(this.contextBuilder.getEatingPlaces(), "", true);
	}

	/**
	 * Go have fun at a shared area
	 */
	public void haveFun() {
		moveToRandomPolygon(this.contextBuilder.getSharedAreas(), "", true);
	}

	/**
	 * Move to random in-out spot and vanish to limbo
	 */
	public void returnHome() {
		moveToRandomInOutSpot("vanishToLimbo");
	}

	/**
	 * Move to random in-out spot
	 */
	public void moveToRandomInOutSpot(String method) {
		HashMap<String, GISPolygon> inOuts = null;
		if (isVehicleUser) {
			inOuts = this.contextBuilder.getVehicleInOuts();
		} else {
			inOuts = this.contextBuilder.getInOuts();
		}
		moveToRandomPolygon(inOuts, method, true);
	}

	/**
	 * Vanish to limbo. A limbo emulates what's off campus.
	 */
	public void vanishToLimbo() {
		GISLimbo limbo = (GISLimbo) Probabilities.getRandomPolygon(this.contextBuilder.getLimbos());
		if (currentPolygon == null)
			currentPolygon = limbo;
		relocate(limbo);
	}

	/**
	 * Plan weekly events
	 */
	public void planWeeklyEvents() {
		scheduleDepartures();
		scheduleLunch();
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (Group group : this.schedule.getGroups()) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				int day = activity.getDay();
				double startTime = activity.getStartTime() - ARRIVAL_DELTA;
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
		this.geography.move(this, destination);
		this.currentPolygon = polygon;
		this.currentPolygon.onRelocation();
		updateActionValueEstimates(polygon.getId());
	}

	/**
	 * Whether or not the student is currently busy learning
	 */
	public boolean isLearning() {
		return this.learning;
	}

	/**
	 * Get last reward
	 */
	public double getLastReward() {
		return this.lastReward;
	}

	/**
	 * Activate learning mode
	 */
	public void activateLearningMode() {
		this.learning = true;
	}

	/**
	 * Deactivate learning mode
	 */
	public void deactivateLearningMode() {
		this.learning = false;
	}

	/**
	 * Schedule departures
	 */
	private void scheduleDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			AcademicActivity lastActivity = schedule.getLastAcademicActivityInDay(day);
			double endTime = lastActivity.getEndTime();
			double ticksToEvent = TickConverter.dayTimeToTicks(day, endTime);
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
			Pair<Double, Double> lunch = Heuristics.getRandomLunchTime(schedule, day);
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
	 * Initialize action's values estimations
	 */
	private void initValueEstimations() {
		this.actionValueEstimates = new HashMap<String, Pair<Double, Integer>>();
		HashMap<String, GISPolygon> sharedAreas = this.contextBuilder.getSharedAreas();
		for (String key : sharedAreas.keySet()) {
			double r = RandomHelper.nextDoubleFromTo(0, 1);
			this.actionValueEstimates.put(key, new Pair<Double, Integer>(r, 0));
		}
		HashMap<String, GISPolygon> eatingPlaces = this.contextBuilder.getEatingPlaces();
		for (String key : eatingPlaces.keySet()) {
			double r = RandomHelper.nextDoubleFromTo(0, 1);
			this.actionValueEstimates.put(key, new Pair<Double, Integer>(r, 0));
		}
		HashMap<String, GISPolygon> inOuts = this.contextBuilder.getInOuts();
		for (String key : inOuts.keySet()) {
			double r = RandomHelper.nextDoubleFromTo(0, 1);
			this.actionValueEstimates.put(key, new Pair<Double, Integer>(r, 0));
		}
		HashMap<String, GISPolygon> vehicleInOuts = this.contextBuilder.getVehicleInOuts();
		for (String key : vehicleInOuts.keySet()) {
			double r = RandomHelper.nextDoubleFromTo(0, 1);
			this.actionValueEstimates.put(key, new Pair<Double, Integer>(r, 0));
		}
	}

	/**
	 * Update action's values estimations based on the incremental sample average
	 * method
	 * 
	 * @param polygonId Id of a polygon
	 */
	private void updateActionValueEstimates(String polygonId) {
		if (this.actionValueEstimates.containsKey(polygonId)) {
			Pair<Double, Integer> estimation = actionValueEstimates.get(polygonId);
			GISDensityMeter densityPolygon = (GISDensityMeter) getPolygonById(polygonId);
			double density = densityPolygon.measureDensity();
			Parameters simParams = RunEnvironment.getInstance().getParameters();
			double socialDistancing = simParams.getDouble("socialDistancing");
			double reference = 1.0 / socialDistancing;
			double R = 0.0;
			if (density < reference) {
				R = 1 - density;
			} else {
				R = -density;
			}
			double Q = estimation.getFirst();
			int N = estimation.getSecond();
			N = N + 1;
			double step = 0.1;
			Q = Q + step * (R - Q);
			estimation.setFirst(Q);
			estimation.setSecond(N);
			this.actionValueEstimates.put(polygonId, estimation);
			this.lastReward = R;
		}
	}

	/**
	 * Move to random polygon
	 * 
	 * @param polygons    Map of polygons to choose from
	 * @param method      Method to call after arriving to polygon
	 * @param weightBased Random weight-based selection
	 */
	private void moveToRandomPolygon(HashMap<String, GISPolygon> polygons, String method, boolean weightBased) {
		GISPolygon selectedPolygon = null;
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		boolean banditApproach = simParams.getBoolean("banditApproach");
		if (banditApproach) {
			selectedPolygon = Probabilities.getRandomPolygonBanditBased(polygons, this.actionValueEstimates);
		} else if (weightBased) {
			selectedPolygon = Probabilities.getRandomPolygonWeightBased(polygons);
		} else {
			selectedPolygon = Probabilities.getRandomPolygon(polygons);
		}
		moveToPolygon(selectedPolygon, method);
	}

	/**
	 * Move to an specific polygon. Find the shortest route and traverse the route
	 * graph.
	 * 
	 * @param polygon Polygon to go to
	 * @param method  Method to call after arriving to polygon
	 */
	private void moveToPolygon(GISPolygon polygon, String method) {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		String source = currentPolygon.getId();
		String sink = polygon.getId();
		Graph<String, DefaultWeightedEdge> routes = this.contextBuilder.getRoutes();
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths = this.contextBuilder.getShortestPaths();
		GraphPath<String, DefaultWeightedEdge> path = shortestPaths.get(source + "-" + sink);
		List<String> vertexes = path.getVertexList();
		List<DefaultWeightedEdge> edges = path.getEdgeList();
		double speed = Probabilities.getRandomWalkingSpeed();
		double totalTime = 0.0;
		for (int i = 0; i < vertexes.size() - 1; i++) {
			String id = vertexes.get(i + 1);
			GISPolygon nextPolygon = getPolygonById(id);
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
	 * Get polygon by id
	 * 
	 * @param id Polygon Id
	 */
	private GISPolygon getPolygonById(String id) {
		HashMap<String, GISPolygon> teachingFacilities = this.contextBuilder.getTeachingFacilities();
		if (teachingFacilities.containsKey(id))
			return teachingFacilities.get(id);

		HashMap<String, GISPolygon> sharedAreas = this.contextBuilder.getSharedAreas();
		if (sharedAreas.containsKey(id))
			return sharedAreas.get(id);

		HashMap<String, GISPolygon> eatingPlaces = this.contextBuilder.getEatingPlaces();
		if (eatingPlaces.containsKey(id))
			return eatingPlaces.get(id);

		HashMap<String, GISPolygon> inOuts = this.contextBuilder.getInOuts();
		if (inOuts.containsKey(id))
			return inOuts.get(id);

		HashMap<String, GISPolygon> vehicleInOuts = this.contextBuilder.getVehicleInOuts();
		if (vehicleInOuts.containsKey(id))
			return vehicleInOuts.get(id);

		HashMap<String, GISPolygon> transitAreas = this.contextBuilder.getTransitAreas();
		if (transitAreas.containsKey(id))
			return transitAreas.get(id);

		HashMap<String, GISPolygon> limbos = this.contextBuilder.getLimbos();
		if (limbos.containsKey(id))
			return limbos.get(id);

		return null;
	}

}