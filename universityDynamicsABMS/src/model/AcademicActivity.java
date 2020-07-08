package model;

public class AcademicActivity {

	/**
	 * Day
	 */
	private int day;

	/**
	 * Start time
	 */
	private double startTime;

	/**
	 * End time
	 */
	private double endTime;

	/**
	 * Teaching facility id
	 */
	private String teachingFacilityId;

	/**
	 * Create a new academic activity
	 * 
	 * @param day                Day
	 * @param startTime          Start time
	 * @param endTime            End time
	 * @param teachingFacilityId Id of teaching facility
	 */
	public AcademicActivity(int day, double startTime, double endTime, String teachingFacilityId) {
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		this.teachingFacilityId = teachingFacilityId;
	}

	/**
	 * Get day
	 */
	public int getDay() {
		return this.day;
	}

	/**
	 * Get start time
	 */
	public double getStartTime() {
		return this.startTime;
	}

	/**
	 * Get end time
	 */
	public double getEndTime() {
		return this.endTime;
	}

	/**
	 * Get id of teaching facility
	 */
	public String getTeachingFacilityId() {
		return this.teachingFacilityId;
	}

}