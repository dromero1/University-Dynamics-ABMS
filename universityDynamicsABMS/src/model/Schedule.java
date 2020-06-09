package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Schedule {

	private ArrayList<Group> groups;

	public Schedule() {
		this.groups = new ArrayList<Group>();
	}

	public Schedule(ArrayList<Group> groups) {
		this.groups = groups;
	}

	public void addGroup(Group group) {
		groups.add(group);
	}

	public ArrayList<Group> getGroups() {
		return groups;
	}

	public ArrayList<Integer> getCampusDays() {
		ArrayList<Integer> campusDays = new ArrayList<Integer>();
		Set<Integer> days = new HashSet<Integer>();
		for (Group group : groups) {
			Set<Integer> activityDays = group.getActivityDays();
			for (Integer day : activityDays) {
				days.add(day);
			}
		}
		for (Integer day : days)
			campusDays.add(day);
		return campusDays;
	}

	public AcademicActivity getFirstAcademicActivityInDay(int day) {
		AcademicActivity firstActivity = null;
		for (Group group : groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day) {
					if (firstActivity == null) {
						firstActivity = activity;
					} else {
						if (firstActivity.getStartTime() > activity.getStartTime()) {
							firstActivity = activity;
						}
					}
				}
			}
		}
		return firstActivity;
	}

	public AcademicActivity getLastAcademicActivityInDay(int day) {
		AcademicActivity lastActivity = null;
		for (Group group : groups) {
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

	public AcademicActivity getNextAcademicActivity(double day, double hour) {
		AcademicActivity nextActivity = null;
		for (Group group : groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day && activity.getStartTime() > hour) {
					if (nextActivity == null) {
						nextActivity = activity;
					} else {
						if (nextActivity.getStartTime() > activity.getStartTime()) {
							nextActivity = activity;
						}
					}
				}
			}
		}
		return nextActivity;
	}

	public boolean collides(int day, double eventStart, double duration) {
		for (Group group : groups) {
			for (AcademicActivity activity : group.getAcademicActivities()) {
				if (activity.getDay() == day) {
					double activityStart = activity.getStartTime();
					double activityEnd = activity.getEndTime();
					double eventEnd = eventStart + duration;
					if ((eventEnd >= activityStart && eventEnd <= activityEnd)
							|| (eventStart >= activityStart && eventStart <= activityEnd)) {
						return true;
					} else {
						continue;
					}
				}
			}
		}
		return false;
	}

	@Override
	public String toString() {
		return "Schedule [groups=" + groups + "]";
	}

}