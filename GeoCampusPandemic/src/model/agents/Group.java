package model.agents;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Group {

	/**
	 * Group id
	 */
	private String id;

	/**
	 * Capacity
	 */
	private int capacity;

	/**
	 * Enrollments count
	 */
	private int enrollmentsCount;

	/**
	 * List of academic activities
	 */
	private List<AcademicActivity> academicActivities;

	/**
	 * Create a new group
	 * 
	 * @param id       Group id
	 * @param capacity Capacity
	 */
	public Group(String id, int capacity) {
		this.id = id;
		this.capacity = capacity;
		this.enrollmentsCount = 0;
		this.academicActivities = new ArrayList<>();
	}

	/**
	 * Add a new academic activity
	 * 
	 * @param day                Day
	 * @param startTime          Start time
	 * @param endTime            End time
	 * @param teachingFacilityId Id of teaching facility
	 */
	public void addAcademicActivity(int day, double startTime, double endTime,
			String teachingFacilityId) {
		AcademicActivity activity = new AcademicActivity(day, startTime,
				endTime, teachingFacilityId);
		this.academicActivities.add(activity);
	}

	/**
	 * Enroll a student in the group. It returns false if the enrollment wasn't
	 * possible and true otherwise.
	 */
	public boolean enroll() {
		boolean enrolled = false;
		if (this.enrollmentsCount < this.capacity) {
			this.enrollmentsCount++;
			enrolled = true;
		}
		return enrolled;
	}

	/**
	 * Get group id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Get capacity
	 */
	public int getCapacity() {
		return this.capacity;
	}

	/**
	 * Get list of academic activities
	 */
	public List<AcademicActivity> getAcademicActivities() {
		return this.academicActivities;
	}

	/**
	 * Get a set of the days in which activities are scheduled
	 */
	public Set<Integer> getActivityDays() {
		Set<Integer> days = new HashSet<>();
		for (AcademicActivity activity : this.academicActivities) {
			int day = activity.getDay();
			days.add(day);
		}
		return days;
	}

}