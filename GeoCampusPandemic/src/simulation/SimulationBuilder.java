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
import gis.GISDensityMeter;
import gis.GISPolygon;
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
	 * In-Out spots
	 */
	public HashMap<String, GISPolygon> inOuts;

	/**
	 * Vehicle in-out spots
	 */
	public HashMap<String, GISPolygon> vehicleInOuts;

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
		this.teachingFacilities = readDensityPolygons(Paths.TEACHING_FACILITIES_GEOMETRY_SHAPEFILE,
				Paths.TEACHING_AREAS_DATABASE);
		for (GISPolygon teachingFacility : this.teachingFacilities.values()) {
			teachingFacility.setGeometryInGeography(geography);
			context.add(teachingFacility);
		}

		// Initialize shared areas
		this.sharedAreas = readDensityPolygons(Paths.SHARED_AREAS_GEOMETRY_SHAPEFILE, Paths.SHARED_AREAS_DATABASE);
		for (GISPolygon sharedArea : this.sharedAreas.values()) {
			sharedArea.setGeometryInGeography(geography);
			context.add(sharedArea);
		}

		// Initialize eating places
		this.eatingPlaces = readDensityPolygons(Paths.EATING_PLACES_GEOMETRY_SHAPEFILE, Paths.EATING_AREAS_DATABASE);
		for (GISPolygon eatingPlace : this.eatingPlaces.values()) {
			eatingPlace.setGeometryInGeography(geography);
			context.add(eatingPlace);
		}

		// Initialize in-outs spots
		this.inOuts = readDensityPolygons(Paths.INOUTS_GEOMETRY_SHAPEFILE, Paths.INOUT_AREAS_DATABASE);
		for (GISPolygon inOut : inOuts.values()) {
			inOut.setGeometryInGeography(geography);
			context.add(inOut);
		}

		// Initialize vehicle in-out spots
		this.vehicleInOuts = readDensityPolygons(Paths.VEHICLE_INOUTS_GEOMETRY_SHAPEFILE,
				Paths.VEHICLE_INOUT_AREAS_DATABASE);
		for (GISPolygon vehicleInOut : vehicleInOuts.values()) {
			vehicleInOut.setGeometryInGeography(geography);
			context.add(vehicleInOut);
		}

		// Initialize transit areas
		this.transitAreas = readDensityPolygons(Paths.TRANSIT_AREAS_GEOMETRY_SHAPEFILE, Paths.TRANSIT_AREAS_DATABASE);
		for (GISPolygon transitArea : transitAreas.values()) {
			transitArea.setGeometryInGeography(geography);
			context.add(transitArea);
		}

		// Initialize other facilities
		HashMap<String, GISPolygon> otherFacitilies = readDensityLessPolygons(
				Paths.OTHER_FACILITIES_GEOMETRY_SHAPEFILE);
		for (GISPolygon otherFacility : otherFacitilies.values()) {
			otherFacility.setGeometryInGeography(geography);
			context.add(otherFacility);
		}

		// Initialize parking lots
		HashMap<String, GISPolygon> parkingLots = readDensityLessPolygons(Paths.PARKING_LOTS_GEOMETRY_SHAPEFILE);
		for (GISPolygon parkingLot : parkingLots.values()) {
			parkingLot.setGeometryInGeography(geography);
			context.add(parkingLot);
		}

		// Initialize limbos
		this.limbos = readDensityLessPolygons(Paths.LIMBOS_GEOMETRY_SHAPEFILE);
		for (GISPolygon limbo : this.limbos.values()) {
			limbo.setGeometryInGeography(geography);
			context.add(limbo);
		}

		// Read routes
		this.routes = Reader.readRoutes(Paths.ROUTES_DATABASE);

		// Find shortest paths
		this.shortestPaths = Heuristics.findShortestPaths(this.routes);

		// Read groups
		HashMap<String, Group> groups = Reader.readGroupsDatabase(Paths.GROUPS_DATABASE);

		// TODO Refactor Read workplaces weights
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
			Schedule schedule = Heuristics.buildRandomSchedule(groups);
			if (schedule != null && schedule.getGroupCount() > 0) {
				student.setSchedule(schedule);
				context.add(student);
			}
		}

		// Add support staff to simulation
		ArrayList<SupportStaff> supportStaff = createSupportStaff(simParams.getInteger("supportStaff"), geography);
		for (SupportStaff staff : supportStaff) {
			context.add(staff);
		}

		return context;
	}

	/**
	 * Create geography projection
	 * 
	 * @param context Simulation context
	 */
	private Geography<Object> createGeographyProjection(Context<Object> context) {
		GeographyParameters<Object> params = new GeographyParameters<Object>();
		GeographyFactory geographyFactory = GeographyFactoryFinder.createGeographyFactory(null);
		Geography<Object> geography = geographyFactory.createGeography("campus", context, params);
		return geography;
	}

	/**
	 * Read geo-spatial campus
	 */
	private GISCampus readCampus() {
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(Paths.CAMPUS_GEOMETRY_SHAPEFILE);
		Geometry geometry = (MultiPolygon) features.get(0).getDefaultGeometry();
		return new GISCampus(geometry);
	}

	/**
	 * Read density polygons
	 * 
	 * @param geometryPath   Path to geometry file
	 * @param attributesPath Path to attributes file
	 */
	private HashMap<String, GISPolygon> readDensityPolygons(String geometryPath, String attributesPath) {
		HashMap<String, GISPolygon> polygons = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(geometryPath);
		HashMap<String, GISDensityMeter> areas = Reader.readFacilityAttributes(attributesPath);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			GISDensityMeter densityMeter = areas.get(id);
			densityMeter.setPolygonId(id);
			densityMeter.setGeometry(geometry);
			polygons.put(id, densityMeter);
		}
		return polygons;
	}

	/**
	 * Read density-less polygons
	 * 
	 * @param geometryPath Path to geometry file
	 */
	private HashMap<String, GISPolygon> readDensityLessPolygons(String geometryPath) {
		HashMap<String, GISPolygon> polygons = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(geometryPath);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
			String id = (String) feature.getAttribute(1);
			GISPolygon polygon = new GISPolygon(id, geometry, 0.0);
			polygons.put(id, polygon);
		}
		return polygons;
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