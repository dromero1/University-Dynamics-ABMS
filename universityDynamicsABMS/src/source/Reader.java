package source;

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
import model.Group;

public class Reader {

	public static ArrayList<SimpleFeature> loadGeometryFromShapefile(String filename) {
		File file = new File(filename);
		try {
			FileDataStore store = FileDataStoreFinder.getDataStore(file);
			SimpleFeatureSource featureSource = store.getFeatureSource();
			SimpleFeatureCollection featureCollection = featureSource.getFeatures();
			SimpleFeatureIterator featureIterator = featureCollection.features();
			ArrayList<SimpleFeature> simpleFeatures = new ArrayList<SimpleFeature>();
			while (featureIterator.hasNext())
				simpleFeatures.add(featureIterator.next());
			featureIterator.close();
			store.dispose();
			return simpleFeatures;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static ArrayList<Group> readGroupsDatabase(String filename) {
		HashMap<String, Group> groups = new HashMap<String, Group>();
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
					continue;
				}
				String[] elements = data.split(";");
				// Group attributes
				String id = "";
				int capacity = 0;
				int day = 0;
				double startTime = 0;
				double endTime = 0;
				String teachingFacilityId = "";
				String room = "";
				// Extract elements
				for (int i = 0; i < elements.length; i++) {
					switch (i) {
					case DBFeatures.SUBJECT_COLUMN:
						id += elements[i];
						break;
					case DBFeatures.GROUP_COLUMN:
						id += elements[i];
						break;
					case DBFeatures.DAY_COLUMN:
						day = Integer.parseInt(elements[i]);
						break;
					case DBFeatures.START_TIME_COLUMN:
						startTime = Double.parseDouble(elements[i]);
						break;
					case DBFeatures.END_TIME_COLUMN:
						endTime = Double.parseDouble(elements[i]);
						break;
					case DBFeatures.CAPACITY_COLUMN:
						capacity = Integer.parseInt(elements[i]);
						break;
					case DBFeatures.TEACHING_FACILITY_COLUMN:
						teachingFacilityId = elements[i];
					case DBFeatures.ROOM_COLUMN:
						room = elements[i];
					default:
						break;
					}
				}
				if (groups.containsKey(id)) {
					Group group = groups.get(id);
					group.addAcademicActivity(day, startTime, endTime, teachingFacilityId, room);
				} else {
					Group group = new Group(id, capacity);
					group.addAcademicActivity(day, startTime, endTime, teachingFacilityId, room);
					groups.put(id, group);
				}
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return new ArrayList<Group>(groups.values());
	}

	public static Graph<String, DefaultWeightedEdge> readRoutes(String filename) {
		Graph<String, DefaultWeightedEdge> routes = new DefaultDirectedWeightedGraph<String, DefaultWeightedEdge>(
				DefaultWeightedEdge.class);
		try {
			File file = new File(filename);
			Scanner scanner = new Scanner(file);
			boolean first = true;
			while (scanner.hasNextLine()) {
				String data = scanner.nextLine();
				if (first) {
					first = false;
					continue;
				}
				String[] elements = data.split(";");
				// Group attributes
				String origin = "";
				String destination = "";
				double weight = 0.0;
				// Extract elements
				for (int i = 0; i < elements.length; i++) {
					switch (i) {
					case DBFeatures.ORIGIN_COLUMN:
						origin = elements[i];
						break;
					case DBFeatures.DESTINATION_COLUMN:
						destination = elements[i];
						break;
					case DBFeatures.DISTANCE_COLUMN:
						weight = Double.parseDouble(elements[i]);
						break;
					default:
						break;
					}
				}
				if (!routes.containsVertex(origin))
					routes.addVertex(origin);
				if (!routes.containsVertex(destination))
					routes.addVertex(destination);
				DefaultWeightedEdge edge = routes.addEdge(origin, destination);
				routes.setEdgeWeight(edge, weight);
			}
			scanner.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return routes;
	}

}