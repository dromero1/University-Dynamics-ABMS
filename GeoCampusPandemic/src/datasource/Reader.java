package datasource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
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
import config.SourceFeatures;
import gis.GISDensityMeter;
import gis.GISPolygon;
import model.agents.Group;

public final class Reader {

	/**
	 * CSV source split regular expression
	 */
	private static final String SOURCE_SPLIT_REGEX = ",";

	/**
	 * Private constructor
	 */
	private Reader() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Load geometry from shapefile
	 * 
	 * @param filename File name
	 */
	public static List<SimpleFeature> loadGeometryFromShapefile(
			String filename) {
		File file = new File(filename);
		try {
			FileDataStore store = FileDataStoreFinder.getDataStore(file);
			SimpleFeatureSource featureSource = store.getFeatureSource();
			SimpleFeatureCollection featureCollection = featureSource
					.getFeatures();
			SimpleFeatureIterator featureIterator = featureCollection
					.features();
			ArrayList<SimpleFeature> simpleFeatures = new ArrayList<>();
			while (featureIterator.hasNext()) {
				simpleFeatures.add(featureIterator.next());
			}
			featureIterator.close();
			store.dispose();
			return simpleFeatures;
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}
		return new ArrayList<>();
	}

	/**
	 * Read groups database
	 * 
	 * @param filename File name
	 */
	public static Map<String, Group> readGroupsDatabase(String filename) {
		Map<String, Group> groups = new HashMap<>();
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					StringBuilder rawGroupId = new StringBuilder();
					int capacity = 0;
					int day = 0;
					double startTime = 0;
					double endTime = 0;
					String teachingFacilityId = null;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.GROUPS_SUBJECT_ID_COLUMN:
							rawGroupId.append(elements[i]);
							break;
						case SourceFeatures.GROUPS_GROUP_ID_COLUMN:
							rawGroupId.append(SourceFeatures.ENTITY_SEPARATOR);
							rawGroupId.append(elements[i]);
							break;
						case SourceFeatures.GROUPS_DAY_COLUMN:
							day = Integer.parseInt(elements[i]);
							break;
						case SourceFeatures.GROUPS_START_TIME_COLUMN:
							startTime = Double.parseDouble(elements[i]);
							break;
						case SourceFeatures.GROUPS_END_TIME_COLUMN:
							endTime = Double.parseDouble(elements[i]);
							break;
						case SourceFeatures.GROUPS_CAPACITY_COLUMN:
							capacity = Integer.parseInt(elements[i]);
							break;
						case SourceFeatures.GROUPS_TEACHING_FACILITY_COLUMN:
							teachingFacilityId = elements[i];
							break;
						default:
							break;
						}
					}
					String groupId = rawGroupId.toString();
					if (groups.containsKey(groupId)) {
						Group group = groups.get(groupId);
						group.addAcademicActivity(day, startTime, endTime,
								teachingFacilityId);
					} else {
						Group group = new Group(groupId, capacity);
						group.addAcademicActivity(day, startTime, endTime,
								teachingFacilityId);
						groups.put(groupId, group);
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return groups;
	}

	/**
	 * Read schedule selection database
	 * 
	 * @param filename File name
	 */
	public static Map<String, List<String>> readScheduleSelectionDatabase(
			String filename) {
		Map<String, List<String>> scheduleSelection = new HashMap<>();
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					String studentId = null;
					StringBuilder rawGroupId = new StringBuilder();
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.SELECTION_STUDENT_ID_COLUMN:
							studentId = elements[i];
							break;
						case SourceFeatures.SELECTION_SUBJECT_ID_COLUMN:
							rawGroupId.append(elements[i]);
							break;
						case SourceFeatures.SELECTION_GROUP_ID_COLUMN:
							rawGroupId.append(SourceFeatures.ENTITY_SEPARATOR);
							rawGroupId.append(elements[i]);
							break;
						default:
							break;
						}
					}
					List<String> selectedGroups = null;
					String groupId = rawGroupId.toString();
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
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return scheduleSelection;
	}

	/**
	 * Read facility attributes database
	 * 
	 * @param filename File name
	 */
	public static Map<String, GISPolygon> readFacilityAttributesDatabase(
			String filename) {
		Map<String, GISPolygon> attributes = new HashMap<>();
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					String id = null;
					double area = 0;
					double weight = 0;
					boolean active = false;
					String link = null;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.FACILITIES_ATTRIBUTES_ID_COLUMN:
							id = elements[i];
							break;
						case SourceFeatures.FACILITIES_ATTRIBUTES_AREA_COLUMN:
							area = Double.parseDouble(elements[i]);
							break;
						case SourceFeatures.FACILITIES_ATTRIBUTES_WEIGHT_COLUMN:
							weight = Double.parseDouble(elements[i]);
							break;
						case SourceFeatures.FACILITIES_ATTRIBUTES_ACTIVE_COLUMN:
							active = elements[i].equals("1");
							break;
						case SourceFeatures.FACILITIES_ATTRIBUTES_LINK_COLUMN:
							link = elements[i];
							break;
						default:
							break;
						}
					}
					GISPolygon polygon = null;
					if (area > 0) {
						polygon = new GISDensityMeter(area, weight, active,
								link);
					} else {
						polygon = new GISPolygon(weight, active, link);
					}
					attributes.put(id, polygon);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return attributes;
	}

	/**
	 * Read workplaces database
	 * 
	 * @param filename File name
	 */
	public static Map<String, Double> readWorkplacesDatabase(String filename) {
		Map<String, Double> workplaces = new HashMap<>();
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					String id = null;
					double weight = 0;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.WORKPLACES_ID_COLUMN:
							id = elements[i];
							break;
						case SourceFeatures.WORKPLACES_WEIGHT_COLUMN:
							weight = Double.parseDouble(elements[i]);
							break;
						default:
							break;
						}
					}
					workplaces.put(id, weight);
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return workplaces;
	}

	/**
	 * Read routes database
	 * 
	 * @param filename File name
	 */
	public static Graph<String, DefaultWeightedEdge> readRoutesDatabase(
			String filename) {
		Graph<String, DefaultWeightedEdge> routes = new DefaultDirectedWeightedGraph<>(
				DefaultWeightedEdge.class);
		File file = new File(filename);
		try (Scanner scanner = new Scanner(file)) {
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
				} else {
					String[] elements = data.split(SOURCE_SPLIT_REGEX);
					String origin = null;
					String destination = null;
					double weight = 0.0;
					for (int i = 0; i < elements.length; i++) {
						switch (i) {
						case SourceFeatures.ROUTES_ORIGIN_COLUMN:
							origin = elements[i];
							break;
						case SourceFeatures.ROUTES_DESTINATION_COLUMN:
							destination = elements[i];
							break;
						case SourceFeatures.ROUTES_DISTANCE_COLUMN:
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
					DefaultWeightedEdge edge1 = routes.addEdge(origin,
							destination);
					if (edge1 != null) {
						routes.setEdgeWeight(edge1, weight);
					}
					DefaultWeightedEdge edge2 = routes.addEdge(destination,
							origin);
					if (edge2 != null) {
						routes.setEdgeWeight(edge2, weight);
					}
				}
			}
		} catch (FileNotFoundException fnfe) {
			fnfe.printStackTrace();
		}
		return routes;
	}

}
