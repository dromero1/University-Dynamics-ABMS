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
import model.DiseaseStage;
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
		ArrayList<Student> students = createStudents(simParams.getInteger("students"));
		for (Student student : students) {
			Schedule schedule = Heuristics.buildRandomSchedule(groups);
			if (schedule != null && schedule.getGroupCount() > 0) {
				student.setSchedule(schedule);
				context.add(student);
			}
		}

		// Add support staff to simulation
		ArrayList<SupportStaff> supportStaff = createSupportStaff(simParams.getInteger("supportStaff"));
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
	 * Read polygons
	 * 
	 * @param geometryPath   Path to geometry file
	 * @param attributesPath Path to attributes file
	 */
	private HashMap<String, GISPolygon> readPolygons(String geometryPath, String attributesPath) {
		HashMap<String, GISPolygon> polygons = new HashMap<String, GISPolygon>();
		List<SimpleFeature> features = Reader.loadGeometryFromShapefile(geometryPath);
		HashMap<String, GISPolygon> attributes = Reader.readFacilityAttributesDatabase(attributesPath);
		for (SimpleFeature feature : features) {
			Geometry geometry = (MultiPolygon) feature.getDefaultGeometry();
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
		HashMap<String, GISPolygon> workplaces = new HashMap<String, GISPolygon>();
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
	 * @param studentCount Number of students to create
	 */
	private ArrayList<Student> createStudents(int studentCount) {
		ArrayList<Student> students = new ArrayList<Student>();
		for (int i = 0; i < studentCount; i++) {
			Student student = new Student(this, DiseaseStage.SUSCEPTIBLE, Integer.toString(i));
			students.add(student);
		}
		return students;
	}

	/**
	 * Create support staff
	 * 
	 * @param staffCount Number of staffers to create
	 */
	private ArrayList<SupportStaff> createSupportStaff(int staffCount) {
		ArrayList<SupportStaff> staff = new ArrayList<SupportStaff>();
		for (int i = 0; i < staffCount; i++) {
			SupportStaff supportStaff = new SupportStaff(this, DiseaseStage.SUSCEPTIBLE);
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