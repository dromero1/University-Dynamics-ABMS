package model;

public class AcademicActivity {

	private int day;
	private double startTime;
	private double endTime;
	private String teachingFacilityId;
	private String room;

	public AcademicActivity(int day, double startTime, double endTime, String teachingFacilityId, String room) {
		this.day = day;
		this.startTime = startTime;
		this.endTime = endTime;
		this.teachingFacilityId = teachingFacilityId;
		this.room = room;
	}

	public int getDay() {
		return this.day;
	}

	public double getStartTime() {
		return this.startTime;
	}

	public double getEndTime() {
		return this.endTime;
	}

	public String getTeachingFacilityId() {
		return this.teachingFacilityId;
	}

	public String getRoom() {
		return this.room;
	}

	@Override
	public String toString() {
		return "AcademicActivity [day=" + day + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", teachingFacilityId=" + teachingFacilityId + ", room=" + room + "]";
	}

}