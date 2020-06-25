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
	 * Room
	 */
	private String room;

	/**
	 * Create a new academic activity
	 * 
	 * @param day                Day
	 * @param startTime          Start time
	 * @param endTime            End time
	 * @param teachingFacilityId Id of teaching facility
	 * @param room               Room
	 */
	public AcademicActivity(int day, double startTime, double endTime, String teachingFacilityId, String room) {
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		this.teachingFacilityId = teachingFacilityId;
		this.room = room;
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

	/**
	 * Get room
	 */
	public String getRoom() {
		return this.room;
	}

}