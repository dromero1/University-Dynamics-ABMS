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
import gis.GISPolygon;
import model.Compartment;
import model.Group;
import model.Heuristics;
import model.Schedule;
import model.Student;
import model.Staffer;
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
	 * Reference to geography projection
	 */
	public Geography<Object> geography;

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
		this.geography = createGeographyProjection(context);
		// Initialize teaching facilities
		this.teachingFacilities = readPolygons(Paths.TEACHING_FACILITIES_GEOMETRY_SHAPEFILE,
				Paths.TEACHING_FACILITIES_ATTRIBUTES_DATABASE);
		for (GISPolygon teachingFacility : this.teachingFacilities.values()) {
			context.add(teachingFacility);
		}
		// Initialize shared areas
		this.sharedAreas = readPolygons(Paths.SHARED_AREAS_GEOMETRY_SHAPEFILE, Paths.SHARED_AREAS_ATTRIBUTES_DATABASE);
		for (GISPolygon sharedArea : this.sharedAreas.values()) {
			context.add(sharedArea);
		}
		// Initialize eating places
		this.eatingPlaces = readPolygons(Paths.EATING_PLACES_GEOMETRY_SHAPEFILE,
				Paths.EATING_PLACES_ATTRIBUTES_DATABASE);
		for (GISPolygon eatingPlace : this.eatingPlaces.values()) {
			context.add(eatingPlace);
		}
		// Initialize in-outs spots
		this.inOuts = readPolygons(Paths.INOUTS_GEOMETRY_SHAPEFILE, Paths.INOUT_SPOTS_ATTRIBUTES_DATABASE);
		for (GISPolygon inOut : inOuts.values()) {
			context.add(inOut);
		}
		// Initialize vehicle in-out spots
		this.vehicleInOuts = readPolygons(Paths.VEHICLE_INOUTS_GEOMETRY_SHAPEFILE,
				Paths.VEHICLE_INOUT_SPOTS_ATTRIBUTES_DATABASE);
		for (GISPolygon vehicleInOut : vehicleInOuts.values()) {
			context.add(vehicleInOut);
		}
		// Initialize transit areas
		this.transitAreas = readPolygons(Paths.TRANSIT_AREAS_GEOMETRY_SHAPEFILE,
				Paths.TRANSIT_AREAS_ATTRIBUTES_DATABASE);
		for (GISPolygon transitArea : transitAreas.values()) {
			context.add(transitArea);
		}
		// Initialize other facilities
		HashMap<String, GISPolygon> otherFacitilies = readPolygons(Paths.OTHER_FACILITIES_GEOMETRY_SHAPEFILE,
				Paths.OTHER_FACILITIES_ATTRIBUTES_DATABASE);
		for (GISPolygon otherFacility : otherFacitilies.values()) {
			context.add(otherFacility);
		}
		// Initialize parking lots
		HashMap<String, GISPolygon> parkingLots = readPolygons(Paths.PARKING_LOTS_GEOMETRY_SHAPEFILE,
				Paths.PARKING_LOTS_ATTRIBUTES_DATABASE);
		for (GISPolygon parkingLot : parkingLots.values()) {
			context.add(parkingLot);
		}
		// Initialize limbos
		this.limbos = readPolygons(Paths.LIMBOS_GEOMETRY_SHAPEFILE, Paths.LIMBOS_ATTRIBUTES_DATABASE);
		for (GISPolygon limbo : this.limbos.values()) {
			context.add(limbo);
		}
		// Initialize workplaces
		this.workplaces = readWorkplaces();
		// Read routes
		this.routes = Reader.readRoutesDatabase(Paths.ROUTES_DATABASE);
		// Find shortest paths
		this.shortestPaths = Heuristics.findShortestPaths(this.routes);
		// Read groups
		HashMap<String, Group> groups = Reader.readGroupsDatabase(Paths.GROUPS_DATABASE);
		// Add students to simulation
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		ArrayList<Student> students = createStudents(simParams.getInteger("susceptibleStudents"),
				simParams.getInteger("infectedStudents"));
		for (Student student : students) {
			Schedule schedule = Heuristics.buildRandomSchedule(groups);
			if (schedule != null && schedule.getGroupCount() > 0) {
				student.setSchedule(schedule);
				context.add(student);
			}
		}
		// Add staffers to simulation
		ArrayList<Staffer> staffers = createStaffers(simParams.getInteger("susceptibleStaffers"),
				simParams.getInteger("infectedStaffers"));
		for (Staffer staff : staffers) {
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
	 * Read polygons
	 * 
	 * @param geometryPath   Path to geometry file
	 * @param attributesPath Path to attributes file
	 */
	private HashMap<String, GISPolygon> readPolygons(String geometryPath, String attributesPath) {
		HashMap<String, GISPolygon> polygons = new HashMap<>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(geometryPath);
		HashMap<String, GISPolygon> attributes = Reader.readFacilityAttributesDatabase(attributesPath);
		for (SimpleFeature feature : features) {
			MultiPolygon multiPolygon = (MultiPolygon) feature.getDefaultGeometry();
			Geometry geometry = multiPolygon.getGeometryN(0);
			String id = (String) feature.getAttribute(1);
			GISPolygon polygon = attributes.get(id);
			polygon.setPolygonId(id);
			polygon.setGeometryInGeography(this.geography, geometry);
			polygons.put(id, polygon);
		}
		return polygons;
	}

	/**
	 * Read workplaces
	 */
	private HashMap<String, GISPolygon> readWorkplaces() {
		HashMap<String, GISPolygon> workplaces = new HashMap<>();
		HashMap<String, Double> places = Reader.readWorkplacesDatabase(Paths.WORKPLACES_DATABASE);
		for (String workplaceId : places.keySet()) {
			GISPolygon polygon = getPolygonById(workplaceId);
			double weight = places.get(workplaceId);
			polygon.setWorkWeight(weight);
			workplaces.put(workplaceId, polygon);
		}
		return workplaces;
	}

	/**
	 * Create students
	 * 
	 * @param susceptibleStudents Number of susceptible students
	 * @param infectedStudents    Number of infected students
	 */
	private ArrayList<Student> createStudents(int susceptibleStudents, int infectedStudents) {
		ArrayList<Student> students = new ArrayList<>();
		for (int i = 0; i < infectedStudents; i++) {
			Student student = new Student(this, Compartment.INFECTED, Integer.toString(i));
			students.add(student);
		}
		for (int i = 0; i < susceptibleStudents; i++) {
			Student student = new Student(this, Compartment.SUSCEPTIBLE, Integer.toString(i));
			students.add(student);
		}
		return students;
	}

	/**
	 * Create staffers
	 * 
	 * @param susceptibleStaffers Number of susceptible staffers
	 * @param infectedStaffers    Number of infected staffers
	 */
	private ArrayList<Staffer> createStaffers(int susceptibleStaffers, int infectedStaffers) {
		ArrayList<Staffer> staffers = new ArrayList<>();
		for (int i = 0; i < susceptibleStaffers; i++) {
			Staffer staffer = new Staffer(this, Compartment.SUSCEPTIBLE);
			staffers.add(staffer);
		}
		for (int i = 0; i < infectedStaffers; i++) {
			Staffer staffer = new Staffer(this, Compartment.INFECTED);
			staffers.add(staffer);
		}
		return staffers;
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