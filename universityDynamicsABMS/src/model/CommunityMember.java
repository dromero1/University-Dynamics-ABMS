package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import gis.GISLimbo;
import gis.GISPolygon;
import repast.simphony.gis.util.GeometryUtil;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.gis.Geography;
import repast.simphony.util.collections.Pair;
import simulation.EventScheduler;
import simulation.SimulationBuilder;
import util.TickConverter;

public abstract class CommunityMember {

	/**
	 * Planning delta (unit: hours)
	 */
	protected static final double PLANNING_DELTA = 1;

	/**
	 * Vehicle user flag. Determines whether the student enters the campus by car or
	 * by foot.
	 */
	protected boolean isVehicleUser;

	/**
	 * Reference to geography projection
	 */
	protected Geography<Object> geography;

	/**
	 * Reference to simulation builder
	 */
	protected SimulationBuilder contextBuilder;

	/**
	 * Current polygon
	 */
	protected GISPolygon currentPolygon;

	/**
	 * Last exit polygon
	 */
	protected GISPolygon lastExit;

	/**
	 * Action's values
	 */
	protected HashMap<Integer, Pair<Double, Integer>> actionValues;

	/**
	 * Last reward
	 */
	protected double lastReward;

	/**
	 * Create a new community member agent
	 * 
	 * @param geography      Reference to geography projection
	 * @param contextBuilder Reference to the simulation builder
	 */
	public CommunityMember(Geography<Object> geography, SimulationBuilder contextBuilder) {
		this.geography = geography;
		this.contextBuilder = contextBuilder;
		this.isVehicleUser = Probabilities.getRandomVehicleUsage();
		initActionValues();
		vanishToLimbo();
	}

	/**
	 * Schedule recurring events
	 */
	public void scheduleRecurringEvents() {
		scheduleActivities();
		scheduleDepartures();
		scheduleLunch();
		scheduleArrivalPlanning();
	}

	/**
	 * Move to random in-out spot and vanish to limbo
	 */
	public void returnHome() {
		this.lastExit = moveToRandomInOutSpot("vanishToLimbo");
	}

	/**
	 * Go have lunch at a designated eating place
	 */
	public void haveLunch() {
		moveToRandomPolygon(this.contextBuilder.eatingPlaces, "", SelectionStrategy.weightBased);
	}

	/**
	 * Vanish to limbo. A limbo emulates what's off campus.
	 */
	public void vanishToLimbo() {
		GISLimbo limbo = (GISLimbo) Probabilities.getRandomPolygon(this.contextBuilder.limbos);
		if (this.currentPolygon == null)
			this.currentPolygon = limbo;
		relocate(limbo);
	}

	/**
	 * Plan arrival at day
	 * 
	 * @param day Day
	 */
	public void planArrival(int day) {
		if (this.lastExit != null) {
			double r = RandomHelper.nextDoubleFromTo(0, 1);
			double epsilon = 0.1;
			int selectedShift = 0;
			if (r < 1 - epsilon) {
				double top = Double.NEGATIVE_INFINITY;
				ArrayList<Integer> ties = new ArrayList<Integer>();
				for (Integer shift : this.actionValues.keySet()) {
					Pair<Double, Integer> estimation = this.actionValues.get(shift);
					double Q = estimation.getFirst();
					if (Q > top) {
						top = Q;
						ties.clear();
						ties.add(shift);
					} else if (Q == top) {
						ties.add(shift);
					}
				}
				Collections.shuffle(ties);
				selectedShift = ties.get(0);
			} else {
				Object[] shifts = this.actionValues.keySet().toArray();
				int i = RandomHelper.nextIntFromTo(0, shifts.length - 1);
				selectedShift = (Integer) shifts[i];
			}
			EventScheduler eventScheduler = EventScheduler.getInstance();
			double ticks = TickConverter.minutesToTicks(selectedShift);
			eventScheduler.scheduleOneTimeEvent(ticks, this, "relocate", this.lastExit);
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
	}

	/**
	 * Get last reward
	 */
	public double getLastReward() {
		return this.lastReward;
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
	 * Schedule arrival planning
	 */
	protected abstract void scheduleArrivalPlanning();

	/**
	 * Move to random polygon
	 * 
	 * @param polygons          Map of polygons to choose from
	 * @param method            Method to call after arriving to polygon
	 * @param selectionStrategy Selection strategy
	 */
	protected GISPolygon moveToRandomPolygon(HashMap<String, GISPolygon> polygons, String method,
			SelectionStrategy strategy) {
		GISPolygon selectedPolygon = null;
		switch (strategy) {
		case weightBased:
			selectedPolygon = Probabilities.getRandomPolygonWeightBased(polygons);
			break;
		default:
			selectedPolygon = Probabilities.getRandomPolygon(polygons);
			break;
		}
		moveToPolygon(selectedPolygon, method);
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
		EventScheduler eventScheduler = EventScheduler.getInstance();
		String source = this.currentPolygon.getId();
		String sink = polygon.getId();
		Graph<String, DefaultWeightedEdge> routes = this.contextBuilder.routes;
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths = this.contextBuilder.shortestPaths;
		GraphPath<String, DefaultWeightedEdge> path = shortestPaths.get(source + "-" + sink);
		List<String> vertexes = path.getVertexList();
		List<DefaultWeightedEdge> edges = path.getEdgeList();
		double speed = Probabilities.getRandomWalkingSpeed();
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
	 * Move to random in-out spot
	 */
	private GISPolygon moveToRandomInOutSpot(String method) {
		HashMap<String, GISPolygon> inOuts = null;
		if (this.isVehicleUser) {
			inOuts = this.contextBuilder.vehicleInOuts;
		} else {
			inOuts = this.contextBuilder.inOuts;
		}
		return moveToRandomPolygon(inOuts, method, SelectionStrategy.random);
	}

	/**
	 * Initialize action's values
	 */
	private void initActionValues() {
		this.actionValues = new HashMap<Integer, Pair<Double, Integer>>();
		for (int i = 0; i < 7; i++) {
			int shift = i * 10;
			double r = RandomHelper.nextDoubleFromTo(-1, 1);
			this.actionValues.put(shift, new Pair<Double, Integer>(r, 0));
		}
	}

}