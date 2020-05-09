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
		return day;
	}

	public double getStartTime() {
		return startTime;
	}

	public double getEndTime() {
		return endTime;
	}

	public String getTeachingFacilityId() {
		return teachingFacilityId;
	}

	public String getRoom() {
		return room;
	}

	@Override
	public String toString() {
		return "AcademicActivity [day=" + day + ", startTime=" + startTime + ", endTime=" + endTime
				+ ", teachingFacilityId=" + teachingFacilityId + ", room=" + room + "]";
	}

}