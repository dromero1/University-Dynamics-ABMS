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
import model.Probabilities;
import model.Student;
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
	private HashMap<String, GISPolygon> teachingFacilities;

	/**
	 * Shared areas
	 */
	private HashMap<String, GISPolygon> sharedAreas;

	/**
	 * Eating places
	 */
	private HashMap<String, GISPolygon> eatingPlaces;

	/**
	 * Other facilities
	 */
	private HashMap<String, GISPolygon> otherFacitilies;

	/**
	 * In-Out spots
	 */
	private HashMap<String, GISPolygon> inOuts;

	/**
	 * Vehicle in-out spots
	 */
	private HashMap<String, GISPolygon> vehicleInOuts;

	/**
	 * Parking lots
	 */
	private HashMap<String, GISPolygon> parkingLots;

	/**
	 * Transit areas
	 */
	private HashMap<String, GISPolygon> transitAreas;

	/**
	 * Limbos
	 */
	private HashMap<String, GISPolygon> limbos;

	/**
	 * Routes
	 */
	private Graph<String, DefaultWeightedEdge> routes;

	/**
	 * Shortest paths between all vertexes
	 */
	private HashMap<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths;

	/**
	 * Build simulation
	 * 
	 * @param context Simulation context
	 */
	@Override
	public Context<Object> build(Context<Object> context) {
		context.setId("universityDynamicsABMS");

		// Create geography projection
		Geography<Object> geography = getGeographyProjection(context);

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

		// Add students to simulation
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		ArrayList<Student> students = createStudents(simParams.getInteger("students"), geography);
		for (Student student : students) {
			student.setSchedule(Heuristics.getRandomSchedule(groups));
			student.planWeeklyEvents();
			context.add(student);
		}

		// Simulation end
		RunEnvironment.getInstance().endAt(144 * 100);

		return context;
	}

	public HashMap<String, GISPolygon> getLimbos() {
		return this.limbos;
	}

	public HashMap<String, GISPolygon> getInOuts() {
		return this.inOuts;
	}

	public HashMap<String, GISPolygon> getVehicleInOuts() {
		return this.vehicleInOuts;
	}

	public HashMap<String, GISPolygon> getTeachingFacilities() {
		return this.teachingFacilities;
	}

	public HashMap<String, GISPolygon> getEatingPlaces() {
		return this.eatingPlaces;
	}

	public HashMap<String, GISPolygon> getSharedAreas() {
		return this.sharedAreas;
	}

	public HashMap<String, GISPolygon> getTransitAreas() {
		return this.transitAreas;
	}

	public Graph<String, DefaultWeightedEdge> getRoutes() {
		return this.routes;
	}

	public HashMap<String, GraphPath<String, DefaultWeightedEdge>> getShortestPaths() {
		return this.shortestPaths;
	}

	private Geography<Object> getGeographyProjection(Context<Object> context) {
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
			boolean isVehicleUser = Probabilities.getRandomVehicleUsage();
			Student student = new Student(geography, this, isVehicleUser);
			students.add(student);
		}
		return students;
	}

}