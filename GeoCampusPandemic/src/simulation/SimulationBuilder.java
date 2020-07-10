package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import config.Paths;
import gis.GISCampus;
import gis.GISEatingPlace;
import gis.GISInOut;
import gis.GISLimbo;
import gis.GISOtherFacility;
import gis.GISParkingLot;
import gis.GISPolygon;
import gis.GISSharedArea;
import gis.GISTeachingFacility;
import gis.GISTransitArea;
import gis.GISVehicleInOut;
import model.Group;
import model.Heuristics;
import model.Schedule;
import model.Student;
import model.SupportStaff;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;
import repast.simphony.util.collections.Pair;
import source.Reader;

public class SimulationBuilder implements ContextBuilder<Object> {

	/**
	 * Teaching facilities
	 */
	public HashMap<String, GISPolygon> teachingFacilities;

	/**
	 * Shared areas
	 */
	public HashMap<String, GISPolygon> sharedAreas;

	/**
	 * Eating places
	 */
	public HashMap<String, GISPolygon> eatingPlaces;

	/**
	 * Other facilities
	 */
	public HashMap<String, GISPolygon> otherFacitilies;

	/**
	 * In-Out spots
	 */
	public HashMap<String, GISPolygon> inOuts;

	/**
	 * Vehicle in-out spots
	 */
	public HashMap<String, GISPolygon> vehicleInOuts;

	/**
	 * Parking lots
	 */
	public HashMap<String, GISPolygon> parkingLots;

	/**
	 * Transit areas
	 */
	public HashMap<String, GISPolygon> transitAreas;

	/**
	 * Limbos
	 */
	public HashMap<String, GISPolygon> limbos;

	/**
	 * Workplaces
	 */
	public HashMap<String, GISPolygon> workplaces;

	/**
	 * Routes
	 */
	public Graph<String, DefaultWeightedEdge> routes;

	/**
	 * Shortest paths between all vertexes
	 */
	public HashMap<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths;

	/**
	 * Build simulation
	 * 
	 * @param context Simulation context
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("GeoCampusPandemic");

		// Create geography projection
		Geography<Object> geography = createGeographyProjection(context);

		// Initialize campus
		GISCampus campus = readCampus();
		campus.setGeometryInGeography(geography);
		context.add(campus);

		// Initialize teaching facilities
		this.teachingFacilities = readTeachingFacilities();
		for (GISPolygon teachingFacility : this.teachingFacilities.values()) {
			teachingFacility.setGeometryInGeography(geography);
			context.add(teachingFacility);
		}

		// Initialize shared areas
		this.sharedAreas = readSharedAreas();
		for (GISPolygon sharedArea : this.sharedAreas.values()) {
			sharedArea.setGeometryInGeography(geography);
			context.add(sharedArea);
		}

		// Initialize eating places
		this.eatingPlaces = readEatingPlaces();
		for (GISPolygon eatingPlace : this.eatingPlaces.values()) {
			eatingPlace.setGeometryInGeography(geography);
			context.add(eatingPlace);
		}

		// Initialize other facilities
		this.otherFacitilies = readOtherFacilities();
		for (GISPolygon otherFacility : this.otherFacitilies.values()) {
			otherFacility.setGeometryInGeography(geography);
			context.add(otherFacility);
		}

		// Initialize parking lots
		this.parkingLots = readParkingLots();
		for (GISPolygon parkingLot : this.parkingLots.values()) {
			parkingLot.setGeometryInGeography(geography);
			context.add(parkingLot);
		}

		// Initialize limbos
		this.limbos = readLimbos();
		for (GISPolygon limbo : this.limbos.values()) {
			limbo.setGeometryInGeography(geography);
			context.add(limbo);
		}

		// Initialize in-outs spots
		this.inOuts = readInOuts();
		for (GISPolygon inOut : inOuts.values()) {
			inOut.setGeometryInGeography(geography);
			context.add(inOut);
		}

		// Initialize vehicle in-out spots
		this.vehicleInOuts = readVehicleInOuts();
		for (GISPolygon vehicleInOut : vehicleInOuts.values()) {
			vehicleInOut.setGeometryInGeography(geography);
			context.add(vehicleInOut);
		}

		// Initialize transit areas
		this.transitAreas = readTransitAreas();
		for (GISPolygon transitArea : transitAreas.values()) {
			transitArea.setGeometryInGeography(geography);
			context.add(transitArea);
		}

		// Read routes
		this.routes = Reader.readRoutes(Paths.ROUTES_DATABASE);

		// Find shortest paths
		this.shortestPaths = Heuristics.findShortestPaths(this.routes);

		// Read groups
		ArrayList<Group> groups = Reader.readGroupsDatabase(Paths.GROUPS_DATABASE);

		// Read schedule selection
		HashMap<String, ArrayList<String>> scheduleSelection = Reader
				.readScheduleSelectionDatabase(Paths.SCHEDULE_SELECTION_DATABASE);

		// Read workplaces weights
		this.workplaces = new HashMap<String, GISPolygon>();
		HashMap<String, Double> workplaces = Reader.readWorkplaces(Paths.WORKPLACES_DATABASE);
		for (String workplaceId : workplaces.keySet()) {
			GISPolygon polygon = getPolygonById(workplaceId);
			double weight = workplaces.get(workplaceId);
			polygon.setWorkWeight(weight);
			this.workplaces.put(workplaceId, polygon);
		}

		// Add students to simulation
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		ArrayList<Student> students = createStudents(simParams.getInteger("students"), geography);
		for (Student student : students) {
			Schedule schedule = Heuristics.getRandomSchedule(groups);
			student.setSchedule(schedule);
			context.add(student);
		}

		// Add support staff to simulation
		ArrayList<SupportStaff> supportStaff = createSupportStaff(simParams.getInteger("supportStaff"), geography);
		for (SupportStaff staff : supportStaff) {
			context.add(staff);
		}

		// Simulation end
		RunEnvironment.getInstance().endAt(168 * 50);

		return context;
	}

	private Geography<Object> createGeographyProjection(Context<Object> context) {
		GeographyParameters<Object> params = new GeographyParameters<Object>();
		GeographyFactory geographyFactory = GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> geography = geographyFactory.createGeography("campus", context, params);
		return geography;
	}

	private GISCampus readCampus() {
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.CAMPUS_GEOMETRY_SHAPEFILE);
		Geometry geometry = (MultiPolygon) features.get(0).getDefaultGeometry();
		return new GISCampus(geometry);
	}

	private HashMap<String, GISPolygon> readLimbos() {
		HashMap<String, GISPolygon> limbos = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.LIMBOS_GEOMETRY_SHAPEFILE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			GISLimbo limbo = new GISLimbo(id, geometry);
			limbos.put(id, limbo);
		}
		return limbos;
	}

	private HashMap<String, GISPolygon> readInOuts() {
		HashMap<String, GISPolygon> inOuts = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.INOUTS_GEOMETRY_SHAPEFILE);
		HashMap<String, Pair<Double, Double>> areas = Reader.readFacilityAreas(Paths.INOUT_AREAS_DATABASE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			double area = areas.get(id).getFirst();
			double weight = areas.get(id).getSecond();
			GISInOut inOut = new GISInOut(id, geometry, area, weight);
			inOuts.put(id, inOut);
		}
		return inOuts;
	}

	private HashMap<String, GISPolygon> readVehicleInOuts() {
		HashMap<String, GISPolygon> vehicleInOuts = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.VEHICLE_INOUTS_GEOMETRY_SHAPEFILE);
		HashMap<String, Pair<Double, Double>> areas = Reader.readFacilityAreas(Paths.VEHICLE_INOUT_AREAS_DATABASE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			double area = areas.get(id).getFirst();
			double weight = areas.get(id).getSecond();
			GISVehicleInOut vehicleInOut = new GISVehicleInOut(id, geometry, area, weight);
			vehicleInOuts.put(id, vehicleInOut);
		}
		return vehicleInOuts;
	}

	private HashMap<String, GISPolygon> readTransitAreas() {
		HashMap<String, GISPolygon> transitAreas = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.TRANSIT_AREAS_GEOMETRY_SHAPEFILE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			GISTransitArea transitArea = new GISTransitArea(id, geometry);
			transitAreas.put(id, transitArea);
		}
		return transitAreas;
	}

	private HashMap<String, GISPolygon> readTeachingFacilities() {
		HashMap<String, GISPolygon> teachingFacilities = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.TEACHING_FACILITIES_GEOMETRY_SHAPEFILE);
		HashMap<String, Pair<Double, Double>> areas = Reader.readFacilityAreas(Paths.TEACHING_AREAS_DATABASE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			double area = areas.get(id).getFirst();
			double weight = areas.get(id).getSecond();
			GISTeachingFacility teachingFacility = new GISTeachingFacility(id, geometry, area, weight);
			teachingFacilities.put(id, teachingFacility);
		}
		return teachingFacilities;
	}

	private HashMap<String, GISPolygon> readSharedAreas() {
		HashMap<String, GISPolygon> sharedAreas = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.SHARED_AREAS_GEOMETRY_SHAPEFILE);
		HashMap<String, Pair<Double, Double>> areas = Reader.readFacilityAreas(Paths.SHARED_AREAS_DATABASE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			double area = areas.get(id).getFirst();
			double weight = areas.get(id).getSecond();
			GISSharedArea sharedArea = new GISSharedArea(id, geometry, area, weight);
			sharedAreas.put(id, sharedArea);
		}
		return sharedAreas;
	}

	private HashMap<String, GISPolygon> readEatingPlaces() {
		HashMap<String, GISPolygon> eatingPlaces = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.EATING_PLACES_GEOMETRY_SHAPEFILE);
		HashMap<String, Pair<Double, Double>> areas = Reader.readFacilityAreas(Paths.EATING_AREAS_DATABASE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			double area = areas.get(id).getFirst();
			double weight = areas.get(id).getSecond();
			GISEatingPlace eatingPlace = new GISEatingPlace(id, geometry, area, weight);
			eatingPlaces.put(id, eatingPlace);
		}
		return eatingPlaces;
	}

	private HashMap<String, GISPolygon> readOtherFacilities() {
		HashMap<String, GISPolygon> otherFacilities = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.OTHER_FACILITIES_GEOMETRY_SHAPEFILE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			GISOtherFacility otherFacility = new GISOtherFacility(id, geometry);
			otherFacilities.put(id, otherFacility);
		}
		return otherFacilities;
	}

	private HashMap<String, GISPolygon> readParkingLots() {
		HashMap<String, GISPolygon> parkingLots = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.PARKING_LOTS_GEOMETRY_SHAPEFILE);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			GISParkingLot parkingLot = new GISParkingLot(id, geometry);
			parkingLots.put(id, parkingLot);
		}
		return parkingLots;
	}

	private ArrayList<Student> createStudents(int studentCount, Geography<Object> geography) {
		ArrayList<Student> students = new ArrayList<Student>();
		for (int i = 0; i < studentCount; i++) {
			Student student = new Student(geography, this, Integer.toString(i));
			students.add(student);
		}
		return students;
	}

	private ArrayList<SupportStaff> createSupportStaff(int staffCount, Geography<Object> geography) {
		ArrayList<SupportStaff> staff = new ArrayList<SupportStaff>();
		for (int i = 0; i < staffCount; i++) {
			SupportStaff supportStaff = new SupportStaff(geography, this);
			staff.add(supportStaff);
		}
		return staff;
	}

	/**
	 * Get polygon by id
	 * 
	 * @param id Polygon Id
	 */
	public GISPolygon getPolygonById(String id) {
		if (this.teachingFacilities.containsKey(id)) {
			return this.teachingFacilities.get(id);
		}
		if (this.sharedAreas.containsKey(id)) {
			return this.sharedAreas.get(id);
		}
		if (this.eatingPlaces.containsKey(id)) {
			return this.eatingPlaces.get(id);
		}
		if (this.inOuts.containsKey(id)) {
			return this.inOuts.get(id);
		}
		if (this.vehicleInOuts.containsKey(id)) {
			return this.vehicleInOuts.get(id);
		}
		if (this.transitAreas.containsKey(id)) {
			return this.transitAreas.get(id);
		}
		if (this.limbos.containsKey(id)) {
			return this.limbos.get(id);
		}
		return null;
	}

}