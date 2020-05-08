package model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Group {

	private String id;
	private int capacity;
	private int enrollments;
	private ArrayList<AcademicActivity> academicActivities;

	public Group(String id, int capacity) {
		this.id = id;
		this.capacity = capacity;
		this.enrollments = 0;
		this.academicActivities = new ArrayList<AcademicActivity>();
	}

	public void addAcademicActivity(int day, double startTime, double endTime, String teachingFacilityId, String room) {
		AcademicActivity activity = new AcademicActivity(day, startTime, endTime, teachingFacilityId, room);
		academicActivities.add(activity);
	}

	public boolean enroll() {
		boolean enrolled = false;
		if (enrollments < capacity) {
			enrollments++;
			enrolled = true;
		}
		return enrolled;
	}

	public String getId() {
		return id;
	}

	public int getCapacity() {
		return capacity;
	}

	public ArrayList<AcademicActivity> getAcademicActivities() {
		return academicActivities;
	}

	public Set<Integer> getActivityDays() {
		Set<Integer> days = new HashSet<Integer>();
		for (AcademicActivity activity : academicActivities) {
			int day = activity.getDay();
			days.add(day);
		}
		return days;
	}

	@Override
	public String toString() {
		return "Group [id=" + id + ", capacity=" + capacity + ", enrollments=" + enrollments + ", academicActivities="
				+ academicActivities + "]";
	}

}