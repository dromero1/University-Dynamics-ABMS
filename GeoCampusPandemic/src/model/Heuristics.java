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
	 * @param groups Available groups
	 */
	public static Schedule buildRandomSchedule(HashMap<String, Group> groups) {
		int toEnroll = Randomizer.getRandomGroupsToEnrollTo();
		int enrolled = 0;
		ArrayList<Group> groupList = new ArrayList<>();
		for (String groupId : groups.keySet()) {
			groupList.add(groups.get(groupId));
		}
		Collections.shuffle(groupList);
		Schedule schedule = new Schedule();
		int i = 0;
		while (enrolled < toEnroll && i < groups.size()) {
			Group group = groupList.get(i);
			if (group.enroll()) {
				schedule.addGroup(group);
				enrolled++;
			}
			i++;
		}
		return schedule;
	}

	/**
	 * Create a heuristic schedule for a student
	 * 
	 * @param studentId         Student id
	 * @param scheduleSelection Heuristic-based schedule selection
	 * @param groups            Available groups
	 */
	public static Schedule buildHeuristicSchedule(String studentId,
			HashMap<String, ArrayList<String>> scheduleSelection, HashMap<String, Group> groups) {
		Schedule schedule = null;
		if (scheduleSelection.containsKey(studentId)) {
			schedule = new Schedule();
			ArrayList<String> selection = scheduleSelection.get(studentId);
			for (String course : selection) {
				if (groups.containsKey(course)) {
					Group group = groups.get(course);
					schedule.addGroup(group);
				}
			}
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
			double lunchTime = Randomizer.getRandomLunchTime();
			double lunchDuration = Randomizer.getRandomLunchDuration();
			if (lunchTime > firstActivity.getStartTime() && lunchTime < lastActivity.getEndTime()) {
				if (schedule.collides(day, lunchTime, lunchDuration)) {
					continue;
				} else {
					lunch = new Pair<>(lunchTime, lunchDuration);
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
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> paths = new HashMap<>();
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