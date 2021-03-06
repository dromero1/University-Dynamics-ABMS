package model.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Schedule {

	/**
	 * List of groups
	 */
	private List<Group> groups;

	/**
	 * Create a new schedule
	 */
	public Schedule() {
		this.groups = new ArrayList<>();
	}

	/**
	 * Create a new academic activity
	 * 
	 * @param groups List of groups
	 */
	public Schedule(List<Group> groups) {
		this.groups = groups;
	}

	/**
	 * Add group to schedule
	 * 
	 * @param group Group
	 */
	public void addGroup(Group group) {
		this.groups.add(group);
	}

	/**
	 * Get list of days in campus
	 */
	public List<Integer> getCampusDays() {
		List<Integer> campusDays = new ArrayList<>();
		Set<Integer> days = new HashSet<>();
		for (Group group : this.groups) {
			Set<Integer> activityDays = group.getActivityDays();
			for (Integer day : activityDays) {
				days.add(day);
			}
		}
		for (Integer day : days) {
			campusDays.add(day);
		}
		return campusDays;
	}

	/**
	 * Get the first academic activity in an specific day
	 * 
	 * @param day Day
	 */
	public AcademicActivity getFirstAcademicActivityInDay(int day) {
		AcademicActivity firstActivity = null;
		for (Group group : this.groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day) {
					if (firstActivity == null) {
						firstActivity = activity;
					} else {
						if (firstActivity.getStartTime() > activity
								.getStartTime()) {
							firstActivity = activity;
						}
					}
				}
			}
		}
		return firstActivity;
	}

	/**
	 * Get the last academic activity in an specific day
	 * 
	 * @param day Day
	 */
	public AcademicActivity getLastAcademicActivityInDay(int day) {
		AcademicActivity lastActivity = null;
		for (Group group : this.groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day) {
					if (lastActivity == null) {
						lastActivity = activity;
					} else {
						if (lastActivity.getEndTime() < activity.getEndTime()) {
							lastActivity = activity;
						}
					}
				}
			}
		}
		return lastActivity;
	}

	/**
	 * Get the next academic activity in an specific day after a certain hour
	 * 
	 * @param day  Day
	 * @param hour Hour
	 */
	public AcademicActivity getNextAcademicActivity(int day, double hour) {
		AcademicActivity nextActivity = null;
		for (Group group : this.groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day
						&& activity.getStartTime() > hour) {
					if (nextActivity == null) {
						nextActivity = activity;
					} else {
						if (nextActivity.getStartTime() > activity
								.getStartTime()) {
							nextActivity = activity;
						}
					}
				}
			}
		}
		return nextActivity;
	}

	/**
	 * Checks whether a proposed event collides with the current schedule
	 * 
	 * @param day        Day
	 * @param eventStart Event start
	 * @param duration   Event duration
	 */
	public boolean collides(int day, double eventStart, double duration) {
		for (Group group : this.groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day) {
					double activityStart = activity.getStartTime();
					double activityEnd = activity.getEndTime();
					double eventEnd = eventStart + duration;
					if ((eventEnd >= activityStart && eventEnd <= activityEnd)
							|| (eventStart >= activityStart
									&& eventStart <= activityEnd)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Get list of groups
	 */
	public List<Group> getGroups() {
		return this.groups;
	}

	/**
	 * Get group count
	 */
	public int getGroupCount() {
		return this.groups.size();
	}

}