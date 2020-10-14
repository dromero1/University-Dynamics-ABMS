package simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.opengis.feature.simple.SimpleFeature;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import config.SourcePaths;
import datasource.Reader;
import gis.GISPolygon;
import model.agents.Group;
import model.agents.Schedule;
import model.agents.Staffer;
import model.agents.Student;
import model.disease.Compartment;
import model.util.Heuristics;
import repast.simphony.context.Context;
import repast.simphony.context.space.gis.GeographyFactory;
import repast.simphony.context.space.gis.GeographyFactoryFinder;
import repast.simphony.dataLoader.ContextBuilder;
import repast.simphony.engine.environment.RunEnvironment;
import repast.simphony.parameter.Parameters;
import repast.simphony.space.gis.Geography;
import repast.simphony.space.gis.GeographyParameters;

public class SimulationBuilder implements ContextBuilder<Object> {

	/**
	 * End tick (unit: hours)
	 */
	public static final double END_TICK = 2880;

	/**
	 * Geography projection id
	 */
	public static final String GEOGRAPHY_PROJECTION_ID = "campus";

	/**
	 * Reference to geography projection
	 */
	public Geography<Object> geography;

	/**
	 * Teaching facilities
	 */
	public Map<String, GISPolygon> teachingFacilities;

	/**
	 * Shared areas
	 */
	public Map<String, GISPolygon> sharedAreas;

	/**
	 * Eating places
	 */
	public Map<String, GISPolygon> eatingPlaces;

	/**
	 * In-Out spots
	 */
	public Map<String, GISPolygon> inOuts;

	/**
	 * Vehicle in-out spots
	 */
	public Map<String, GISPolygon> vehicleInOuts;

	/**
	 * Transit areas
	 */
	public Map<String, GISPolygon> transitAreas;

	/**
	 * Parking lots
	 */
	public Map<String, GISPolygon> parkingLots;

	/**
	 * Limbos
	 */
	public Map<String, GISPolygon> limbos;

	/**
	 * Workplaces
	 */
	public Map<String, GISPolygon> workplaces;

	/**
	 * Routes
	 */
	public Graph<String, DefaultWeightedEdge> routes;

	/**
	 * Shortest paths between all vertexes
	 */
	public Map<String, GraphPath<String, DefaultWeightedEdge>> shortestPaths;

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
		this.teachingFacilities = readPolygons(SourcePaths.TEACHING_FACILITIES_GEOMETRY_SHAPEFILE,
				SourcePaths.TEACHING_FACILITIES_ATTRIBUTES_DATABASE);
		for (GISPolygon teachingFacility : this.teachingFacilities.values()) {
			context.add(teachingFacility);
		}
		// Initialize shared areas
		this.sharedAreas = readPolygons(SourcePaths.SHARED_AREAS_GEOMETRY_SHAPEFILE,
				SourcePaths.SHARED_AREAS_ATTRIBUTES_DATABASE);
		for (GISPolygon sharedArea : this.sharedAreas.values()) {
			context.add(sharedArea);
		}
		// Initialize eating places
		this.eatingPlaces = readPolygons(SourcePaths.EATING_PLACES_GEOMETRY_SHAPEFILE,
				SourcePaths.EATING_PLACES_ATTRIBUTES_DATABASE);
		for (GISPolygon eatingPlace : this.eatingPlaces.values()) {
			context.add(eatingPlace);
		}
		// Initialize in-outs spots
		this.inOuts = readPolygons(SourcePaths.INOUTS_GEOMETRY_SHAPEFILE, SourcePaths.INOUT_SPOTS_ATTRIBUTES_DATABASE);
		for (GISPolygon inOut : inOuts.values()) {
			context.add(inOut);
		}
		// Initialize vehicle in-out spots
		this.vehicleInOuts = readPolygons(SourcePaths.VEHICLE_INOUTS_GEOMETRY_SHAPEFILE,
				SourcePaths.VEHICLE_INOUT_SPOTS_ATTRIBUTES_DATABASE);
		for (GISPolygon vehicleInOut : vehicleInOuts.values()) {
			context.add(vehicleInOut);
		}
		// Initialize transit areas
		this.transitAreas = readPolygons(SourcePaths.TRANSIT_AREAS_GEOMETRY_SHAPEFILE,
				SourcePaths.TRANSIT_AREAS_ATTRIBUTES_DATABASE);
		for (GISPolygon transitArea : transitAreas.values()) {
			context.add(transitArea);
		}
		// Initialize parking lots
		this.parkingLots = readPolygons(SourcePaths.PARKING_LOTS_GEOMETRY_SHAPEFILE,
				SourcePaths.PARKING_LOTS_ATTRIBUTES_DATABASE);
		for (GISPolygon parkingLot : parkingLots.values()) {
			context.add(parkingLot);
		}
		// Initialize limbos
		this.limbos = readPolygons(SourcePaths.LIMBOS_GEOMETRY_SHAPEFILE, SourcePaths.LIMBOS_ATTRIBUTES_DATABASE);
		for (GISPolygon limbo : this.limbos.values()) {
			context.add(limbo);
		}
		// Initialize other facilities
		Map<String, GISPolygon> otherFacitilies = readPolygons(SourcePaths.OTHER_FACILITIES_GEOMETRY_SHAPEFILE,
				SourcePaths.OTHER_FACILITIES_ATTRIBUTES_DATABASE);
		for (GISPolygon otherFacility : otherFacitilies.values()) {
			context.add(otherFacility);
		}
		// Initialize workplaces
		this.workplaces = readWorkplaces();
		// Read routes
		this.routes = Reader.readRoutesDatabase(SourcePaths.ROUTES_DATABASE);
		// Find shortest paths
		this.shortestPaths = Heuristics.findShortestPaths(this.routes);
		// Read groups
		Map<String, Group> groups = Reader.readGroupsDatabase(SourcePaths.GROUPS_DATABASE);
		// Add students to simulation
		Parameters simParams = RunEnvironment.getInstance().getParameters();
		List<Student> students = createStudents(simParams.getInteger("susceptibleStudents"),
				simParams.getInteger("infectedStudents"));
		for (Student student : students) {
			Schedule schedule = Heuristics.buildRandomSchedule(groups);
			if (schedule != null && schedule.getGroupCount() > 0) {
				student.setSchedule(schedule);
				context.add(student);
			}
		}
		// Add staffers to simulation
		List<Staffer> staffers = createStaffers(simParams.getInteger("susceptibleStaffers"),
				simParams.getInteger("infectedStaffers"));
		for (Staffer staff : staffers) {
			context.add(staff);
		}
		// Set end tick
		RunEnvironment.getInstance().endAt(END_TICK);
		return context;
	}

	/**
	 * Create geography projection
	 * 
	 * @param context Simulation context
	 */
	private Geography<Object> createGeographyProjection(Context<Object> context) {
		GeographyParameters<Object> params = new GeographyParameters<>();
		GeographyFactory geographyFactory = GeographyFactoryFinder.createGeographyFactory(null);
		return geographyFactory.createGeography(GEOGRAPHY_PROJECTION_ID, context, params);
	}

	/**
	 * Read polygons
	 * 
	 * @param geometryPath   Path to geometry file
	 * @param attributesPath Path to attributes file
	 */
	private Map<String, GISPolygon> readPolygons(String geometryPath, String attributesPath) {
		Map<String, GISPolygon> polygons = new HashMap<>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(geometryPath);
		Map<String, GISPolygon> attributes = Reader.readFacilityAttributesDatabase(attributesPath);
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
	private Map<String, GISPolygon> readWorkplaces() {
		Map<String, GISPolygon> placesToWork = new HashMap<>();
		Map<String, Double> places = Reader.readWorkplacesDatabase(SourcePaths.WORKPLACES_DATABASE);
		for (Map.Entry<String, Double> workplace : places.entrySet()) {
			String workplaceId = workplace.getKey();
			GISPolygon polygon = getPolygonById(workplaceId);
			double weight = workplace.getValue();
			polygon.setWorkWeight(weight);
			placesToWork.put(workplaceId, polygon);
		}
		return placesToWork;
	}

	/**
	 * Create students
	 * 
	 * @param susceptibleStudents Number of susceptible students
	 * @param infectedStudents    Number of infected students
	 */
	private List<Student> createStudents(int susceptibleStudents, int infectedStudents) {
		List<Student> students = new ArrayList<>();
		double outbreakTick = ParametersAdapter.getOutbreakTick();
		for (int i = 0; i < infectedStudents; i++) {
			Student student = new Student(this, Compartment.INFECTED, Integer.toString(i), outbreakTick);
			students.add(student);
		}
		for (int i = 0; i < susceptibleStudents; i++) {
			Student student = new Student(this, Compartment.SUSCEPTIBLE, Integer.toString(i), outbreakTick);
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
	private List<Staffer> createStaffers(int susceptibleStaffers, int infectedStaffers) {
		List<Staffer> staffers = new ArrayList<>();
		double outbreakTick = ParametersAdapter.getOutbreakTick();
		for (int i = 0; i < infectedStaffers; i++) {
			Staffer staffer = new Staffer(this, Compartment.INFECTED, outbreakTick);
			staffers.add(staffer);
		}
		for (int i = 0; i < susceptibleStaffers; i++) {
			Staffer staffer = new Staffer(this, Compartment.SUSCEPTIBLE, outbreakTick);
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
		if (this.parkingLots.containsKey(id)) {
			return this.parkingLots.get(id);
		}
		if (this.limbos.containsKey(id)) {
			return this.limbos.get(id);
		}
		return null;
	}

}