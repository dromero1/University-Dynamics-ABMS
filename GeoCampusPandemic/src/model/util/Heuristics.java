package model.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.jgrapht.Graph;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.interfaces.ShortestPathAlgorithm.SingleSourcePaths;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.DefaultWeightedEdge;
import config.SourceFeatures;
import model.agents.AcademicActivity;
import model.agents.Group;
import model.agents.Schedule;
import repast.simphony.util.collections.Pair;

public final class Heuristics {

	/**
	 * Number of trials for lunch time assignment
	 */
	private static final int TRIALS_4_LUNCH_TIME_ASSIGNMENT = 100;

	/**
	 * Private constructor
	 */
	private Heuristics() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Create a new random schedule for a student
	 * 
	 * @param groups Available groups
	 */
	public static Schedule buildRandomSchedule(Map<String, Group> groups) {
		int toEnroll = Randomizer.getRandomGroupsToEnrollTo();
		int enrolled = 0;
		List<Group> groupList = new ArrayList<>();
		for (Map.Entry<String, Group> group : groups.entrySet()) {
			groupList.add(group.getValue());
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
			Map<String, ArrayList<String>> scheduleSelection,
			Map<String, Group> groups) {
		Schedule schedule = null;
		if (scheduleSelection.containsKey(studentId)) {
			schedule = new Schedule();
			List<String> selection = scheduleSelection.get(studentId);
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
	public static Pair<Double, Double> getRandomLunchTime(Schedule schedule,
			int day) {
		Pair<Double, Double> lunch = null;
		AcademicActivity firstActivity = schedule
				.getFirstAcademicActivityInDay(day);
		AcademicActivity lastActivity = schedule
				.getLastAcademicActivityInDay(day);
		for (int i = 0; i < TRIALS_4_LUNCH_TIME_ASSIGNMENT; i++) {
			double lunchTime = Randomizer.getRandomLunchTime();
			double lunchDuration = Randomizer.getRandomLunchDuration();
			if (lunchTime > firstActivity.getStartTime()
					&& lunchTime < lastActivity.getEndTime()
					&& !schedule.collides(day, lunchTime, lunchDuration)) {
				lunch = new Pair<>(lunchTime, lunchDuration);
				break;
			}
		}
		return lunch;
	}

	/**
	 * Find shortest paths between all points using Dijkstra's algorithm
	 * 
	 * @param routes Graph of routes
	 */
	public static Map<String, GraphPath<String, DefaultWeightedEdge>> findShortestPaths(
			Graph<String, DefaultWeightedEdge> routes) {
		HashMap<String, GraphPath<String, DefaultWeightedEdge>> paths = new HashMap<>();
		DijkstraShortestPath<String, DefaultWeightedEdge> dijkstraAlg = new DijkstraShortestPath<>(
				routes);
		for (String source : routes.vertexSet()) {
			SingleSourcePaths<String, DefaultWeightedEdge> iPaths = dijkstraAlg
					.getPaths(source);
			for (String sink : routes.vertexSet()) {
				String id = source + SourceFeatures.ENTITY_SEPARATOR + sink;
				if (!paths.containsKey(id)) {
					GraphPath<String, DefaultWeightedEdge> path = iPaths
							.getPath(sink);
					paths.put(id, path);
				}
			}
		}
		return paths;
	}

}