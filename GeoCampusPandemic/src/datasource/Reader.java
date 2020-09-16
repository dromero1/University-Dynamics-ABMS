package datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import org.geotools.data.FileDataStore;
import org.geotools.data.FileDataStoreFinder;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.opengis.feature.simple.SimpleFeature;
import config.DBFeatures;
import gis.GISDensityMeter;
import gis.GISPolygon;
import model.agents.Group;

public class Reader {

	/**
	 * Load geometry from shapefile
	 * 
	 * @param filename File name
	 */
	public static ArrayList<SimpleFeature> loadGeometryFromShapefile(String filename) {
		File file = new File(filename);
		try {
			FileDataStore store = FileDataStoreFinder.getDataStore(file);
			SimpleFeatureSource featureSource = store.getFeatureSource();
			SimpleFeatureCollection featureCollection = featureSource.getFeatures();
			SimpleFeatureIterator featureIterator = featureCollection.features();
			ArrayList<SimpleFeature> simpleFeatures = new ArrayList<>();
			while (featureIterator.hasNext()) {
				simpleFeatures.add(featureIterator.next());
			}
			featureIterator.close();
			store.dispose();
			return simpleFeatures;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Read groups database
	 * 
	 * @param filename File name
	 */
	public static HashMap<String, Group> readGroupsDatabase(String filename) {
		HashMap<String, Group> groups = new HashMap<>();
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(";");
					String groupId = null;
					int capacity = 0;
					int day = 0;
					double startTime = 0;
					double endTime = 0;
					String teachingFacilityId = null;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case DBFeatures.GROUPS_SUBJECT_ID_COLUMN:
							groupId += elements[i];
							break;
						case DBFeatures.GROUPS_GROUP_ID_COLUMN:
							groupId += "-" + elements[i];
							break;
						case DBFeatures.GROUPS_DAY_COLUMN:
							day = Integer.parseInt(elements[i]);
							break;
						case DBFeatures.GROUPS_START_TIME_COLUMN:
							startTime = Double.parseDouble(elements[i]);
							break;
						case DBFeatures.GROUPS_END_TIME_COLUMN:
							endTime = Double.parseDouble(elements[i]);
							break;
						case DBFeatures.GROUPS_CAPACITY_COLUMN:
							capacity = Integer.parseInt(elements[i]);
							break;
						case DBFeatures.GROUPS_TEACHING_FACILITY_COLUMN:
							teachingFacilityId = elements[i];
						default:
							break;
						}
					}
					if (groups.containsKey(groupId)) {
						Group group = groups.get(groupId);
						group.addAcademicActivity(day, startTime, endTime, teachingFacilityId);
					} else {
						Group group = new Group(groupId, capacity);
						group.addAcademicActivity(day, startTime, endTime, teachingFacilityId);
						groups.put(groupId, group);
					}
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return groups;
	}

	/**
	 * Read schedule selection database
	 * 
	 * @param filename File name
	 */
	public static HashMap<String, ArrayList<String>> readScheduleSelectionDatabase(String filename) {
		HashMap<String, ArrayList<String>> scheduleSelection = new HashMap<>();
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(";");
					String studentId = null;
					String groupId = null;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case DBFeatures.SELECTION_STUDENT_ID_COLUMN:
							studentId = elements[i];
							break;
						case DBFeatures.SELECTION_SUBJECT_ID_COLUMN:
							groupId += elements[i];
							break;
						case DBFeatures.SELECTION_GROUP_ID_COLUMN:
							groupId += "-" + elements[i];
							break;
						default:
							break;
						}
					}
					ArrayList<String> selectedGroups = null;
					if (scheduleSelection.containsKey(studentId)) {
						selectedGroups = scheduleSelection.get(studentId);
						if (!selectedGroups.contains(groupId)) {
							selectedGroups.add(groupId);
						}
					} else {
						selectedGroups = new ArrayList<>();
						selectedGroups.add(groupId);
					}
					scheduleSelection.put(studentId, selectedGroups);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return scheduleSelection;
	}

	/**
	 * Read facility attributes database
	 * 
	 * @param filename File name
	 */
	public static HashMap<String, GISPolygon> readFacilityAttributesDatabase(String filename) {
		HashMap<String, GISPolygon> attributes = new HashMap<>();
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(";");
					String id = null;
					double area = 0;
					double weight = 0;
					boolean active = false;
					String link = null;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case DBFeatures.FACILITIES_ATTRIBUTES_ID_COLUMN:
							id = elements[i];
							break;
						case DBFeatures.FACILITIES_ATTRIBUTES_AREA_COLUMN:
							area = Double.parseDouble(elements[i]);
							break;
						case DBFeatures.FACILITIES_ATTRIBUTES_WEIGHT_COLUMN:
							weight = Double.parseDouble(elements[i]);
							break;
						case DBFeatures.FACILITIES_ATTRIBUTES_ACTIVE_COLUMN:
							active = elements[i].equals("1");
							break;
						case DBFeatures.FACILITIES_ATTRIBUTES_LINK_COLUMN:
							link = elements[i];
							break;
						default:
							break;
						}
					}
					GISPolygon polygon = null;
					if (area > 0.0) {
						polygon = new GISDensityMeter(area, weight, active, link);
					} else {
						polygon = new GISPolygon(weight, active, link);
					}
					attributes.put(id, polygon);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return attributes;
	}

	/**
	 * Read workplaces database
	 * 
	 * @param filename File name
	 */
	public static HashMap<String, Double> readWorkplacesDatabase(String filename) {
		HashMap<String, Double> workplaces = new HashMap<>();
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(";");
					String id = null;
					double weight = 0;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case DBFeatures.WORKPLACES_ID_COLUMN:
							id = elements[i];
							break;
						case DBFeatures.WORKPLACES_WEIGHT_COLUMN:
							weight = Double.parseDouble(elements[i]);
							break;
						default:
							break;
						}
					}
					workplaces.put(id, weight);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return workplaces;
	}

	/**
	 * Read routes database
	 * 
	 * @param filename File name
	 */
	public static Graph<String, DefaultWeightedEdge> readRoutesDatabase(String filename) {
		Graph<String, DefaultWeightedEdge> routes = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(";");
					String origin = null;
					String destination = null;
					double weight = 0.0;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case DBFeatures.ROUTES_ORIGIN_COLUMN:
							origin = elements[i];
							break;
						case DBFeatures.ROUTES_DESTINATION_COLUMN:
							destination = elements[i];
							break;
						case DBFeatures.ROUTES_DISTANCE_COLUMN:
							weight = Double.parseDouble(elements[i]);
							break;
						default:
							break;
						}
					}
					if (!routes.containsVertex(origin)) {
						routes.addVertex(origin);
					}
					if (!routes.containsVertex(destination)) {
						routes.addVertex(destination);
					}
					DefaultWeightedEdge edge1 = routes.addEdge(origin, destination);
					if (edge1 != null) {
						routes.setEdgeWeight(edge1, weight);
					}
					DefaultWeightedEdge edge2 = routes.addEdge(destination, origin);
					if (edge2 != null) {
						routes.setEdgeWeight(edge2, weight);
					}
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return routes;
	}

}