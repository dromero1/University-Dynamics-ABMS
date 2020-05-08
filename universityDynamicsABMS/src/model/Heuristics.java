package model;

import java.util.ArrayList;
import java.util.Collections;

public class Heuristics {

	public static Schedule getRandomSchedule(ArrayList<Group> groups) {
		int toEnroll = 6;
		int enrolled = 0;
		Collections.shuffle(groups);
		Schedule schedule = new Schedule();
		int i = 0;
		while (enrolled < toEnroll && i < groups.size()) {
			Group group = groups.get(i);
			if (group.enroll()) {
				schedule.addGroup(group);
				enrolled++;
			}
			i++;
		}
		return schedule;
	}

}