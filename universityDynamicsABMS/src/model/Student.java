package model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import gis.GISEatingPlace;
import gis.GISInOut;
import gis.GISLimbo;
import gis.GISPolygon;
import gis.GISSharedArea;
import gis.GISTeachingFacility;
import gis.GISTransitArea;
import gis.GISVehicleInOut;
import repast.simphony.essentials.RepastEssentials;
import repast.simphony.gis.util.GeometryUtil;
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
		HashMap<String, GISTeachingFacility> teachingFacilities = this.contextBuilder.getTeachingFacilities();
		GISTeachingFacility teachingFacility = teachingFacilities.get(teachingFacilityId);
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
		double[] dayTime = TickConverter.tickToDayTime(tick);
		double day = dayTime[0];
		double hour = dayTime[1];
		AcademicActivity nextActivity = this.schedule.getNextAcademicActivity(day, hour);
		if (nextActivity != null) {
			double delta = nextActivity.getStartTime() - hour;
			if (delta < ARRIVAL_DELTA) {
				attendActivity(nextActivity.getTeachingFacilityId());
			} else {
				EventScheduler eventScheduler = EventScheduler.getInstance();
				eventScheduler.scheduleOneTimeEvent(ARRIVAL_DELTA, this, "haveFun");
			}
		}
	}

	/**
	 * Go have lunch at a designated eating place
	 */
	public void haveLunch() {
		Object[] eatingPlaces = this.contextBuilder.getEatingPlaces().values().toArray();
		moveToRandomPolygon(eatingPlaces, "", true);
	}

	/**
	 * Go have fun at a shared area
	 */
	public void haveFun() {
		Object[] sharedAreas = this.contextBuilder.getSharedAreas().values().toArray();
		moveToRandomPolygon(sharedAreas, "", true);
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
		Object[] inOuts = null;
		if (isVehicleUser) {
			inOuts = this.contextBuilder.getVehicleInOuts().values().toArray();
		} else {
			inOuts = this.contextBuilder.getInOuts().values().toArray();
		}
		moveToRandomPolygon(inOuts, method, false);
	}

	/**
	 * Vanish to limbo. A limbo emulates what's off campus.
	 */
	public void vanishToLimbo() {
		Object[] limbos = this.contextBuilder.getLimbos().values().toArray();
		int i = RandomHelper.nextIntFromTo(0, limbos.length - 1);
		GISLimbo limbo = (GISLimbo) limbos[i];
		if (currentPolygon == null) {
			currentPolygon = limbo;
			relocate(currentPolygon);
		} else {
			moveToPolygon(limbo, "");
		}
	}

	/**
	 * Plan weekly events
	 */
	public void planWeeklyEvents() {
		// Schedule simple events
		scheduleArrivalsAndDepartures();
		scheduleLunch();
		// Schedule activity timetable
		EventScheduler eventScheduler = EventScheduler.getInstance();
		for (Group group : this.schedule.getGroups()) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				// Schedule activity attendance
				int day = activity.getDay();
				double startTime = activity.getStartTime();
				String teachingFacilityId = activity.getTeachingFacilityId();
				double ticksToEvent = TickConverter.dayTimeToTicks(day, startTime);
				eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
						"attendActivity", teachingFacilityId);
				// Schedule leaving the activity
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
	}

	/**
	 * Whether or not the student is currently busy learning
	 */
	public boolean isLearning() {
		return this.learning;
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
	 * Schedule arrivals and departures
	 */
	private void scheduleArrivalsAndDepartures() {
		EventScheduler eventScheduler = EventScheduler.getInstance();
		ArrayList<Integer> days = this.schedule.getCampusDays();
		for (Integer day : days) {
			AcademicActivity firstActivity = schedule.getFirstAcademicActivityInDay(day);
			double startTime = firstActivity.getStartTime() - ARRIVAL_DELTA;
			double ticksToEvent = TickConverter.dayTimeToTicks(day, startTime);
			eventScheduler.scheduleRecurringEvent(ticksToEvent, this, TickConverter.TICKS_PER_WEEK,
					"moveToRandomInOutSpot", "");
			AcademicActivity lastActivity = schedule.getLastAcademicActivityInDay(day);
			double endTime = lastActivity.getEndTime();
			ticksToEvent = TickConverter.dayTimeToTicks(day, endTime);
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
	 * Move to random polygon
	 * 
	 * @param polygons    Array of polygons to choose
	 * @param method      Method to call after arriving to polygon
	 * @param weightBased Random weight-based selection
	 */
	private void moveToRandomPolygon(Object[] polygons, String method, boolean weightBased) {
		GISPolygon selectedPolygon = null;
		if (!weightBased) {
			int i = RandomHelper.nextIntFromTo(0, polygons.length - 1);
			selectedPolygon = (GISPolygon) polygons[i];
		} else {
			selectedPolygon = Probabilities.getRandomPolygonWeightBased(polygons);
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
		Graph<String, DefaultWeightedEdge> routes = this.contextBuilder.getRoutes();
		// Select source and sink
		String source = currentPolygon.getId();
		String sink = polygon.getId();
		// Find shortest path
		DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(routes);
		SingleSourcePaths<String, DefaultWeightedEdge> iPaths = dijkstraAlg.getPaths(source);
		GraphPath<String, DefaultWeightedEdge> path = iPaths.getPath(sink);
		List<String> vertexes = path.getVertexList();
		List<DefaultWeightedEdge> edges = path.getEdgeList();
		// Select random walking speed
		double speed = Probabilities.getRandomWalkingSpeed();
		// Traverse path
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
		// Schedule method
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
		HashMap<String, GISTeachingFacility> teachingFacilities = this.contextBuilder.getTeachingFacilities();
		if (teachingFacilities.containsKey(id))
			return teachingFacilities.get(id);

		HashMap<String, GISSharedArea> sharedAreas = this.contextBuilder.getSharedAreas();
		if (sharedAreas.containsKey(id))
			return sharedAreas.get(id);

		HashMap<String, GISEatingPlace> eatingPlaces = this.contextBuilder.getEatingPlaces();
		if (eatingPlaces.containsKey(id))
			return eatingPlaces.get(id);

		HashMap<String, GISInOut> inOuts = this.contextBuilder.getInOuts();
		if (inOuts.containsKey(id))
			return inOuts.get(id);

		HashMap<String, GISVehicleInOut> vehicleInOuts = this.contextBuilder.getVehicleInOuts();
		if (vehicleInOuts.containsKey(id))
			return vehicleInOuts.get(id);

		HashMap<String, GISTransitArea> transitAreas = this.contextBuilder.getTransitAreas();
		if (transitAreas.containsKey(id))
			return transitAreas.get(id);

		HashMap<String, GISLimbo> limbos = this.contextBuilder.getLimbos();
		if (limbos.containsKey(id))
			return limbos.get(id);

		return null;
	}

}