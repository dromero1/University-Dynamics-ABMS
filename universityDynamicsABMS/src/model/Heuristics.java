package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;

import repast.simphony.util.collections.Pair;

public class Heuristics {

	/**
	 * Create a new random schedule for a student
	 * 
	 * @param groups List of available groups
	 */
	public static Schedule getRandomSchedule(ArrayList<Group> groups) {
		int toEnroll = Probabilities.getRandomGroupsToEnrollTo();
		int enrolled = 0;
		Collections.shuffle(groups);
		Schedule schedule = new Schedule();
		int i = 0;
		while (enrolled < toEnroll && i < groups.size()) {
			Group group = groups.get(i);
			if (group.enroll()) {
				schedule.addGroup(group);
				enrolled++;
			}
			i++;
		}
		return schedule;
	}

	/**
	 * Select a random time to lunch at specific day
	 * 
	 * @param schedule Student's schedule
	 * @param day      Day
	 */
	public static Pair<Double, Double> getRandomLunchTime(Schedule schedule, int day) {
		Pair<Double, Double> lunch = null;
		AcademicActivity firstActivity = schedule.getFirstAcademicActivityInDay(day);
		AcademicActivity lastActivity = schedule.getLastAcademicActivityInDay(day);
		for (int i = 0; i < 100; i++) {
			double lunchTime = Probabilities.getRandomLunchTime();
			double lunchDuration = Probabilities.getRandomLunchDuration();
			if (lunchTime > firstActivity.getStartTime() && lunchTime < lastActivity.getEndTime()) {
				if (schedule.collides(day, lunchTime, lunchDuration)) {
					continue;
				} else {
					lunch = new Pair<Double, Double>(lunchTime, lunchDuration);
					break;
				}
			}
		}
		return lunch;
	}

	/**
	 * Find shortest paths between all points using Dijkstra's algorithm
	 * 
	 * @param routes Graph of routes
	 */
	public static HashMap<String, GraphPath<String, DefaultWeightedEdge>> findShortestPaths(
			Graph<String, DefaultWeightedEdge> routes) {
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> paths = new HashMap<String, GraphPath<String, DefaultWeightedEdge>>();
		DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(routes);
		for (String source : routes.vertexSet()) {
			SingleSourcePaths<String, DefaultWeightedEdge> iPaths = dijkstraAlg.getPaths(source);
			for (String sink : routes.vertexSet()) {
				String id = source + "-" + sink;
				if (!paths.containsKey(id)) {
					GraphPath<String, DefaultWeightedEdge> path = iPaths.getPath(sink);
					paths.put(id, path);
				}
			}
		}
		return paths;
	}

}