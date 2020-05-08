package gis;

import com.vividsolutions.jts.geom.Geometry;
import model.Student;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.query.space.gis.ContainsQuery;

public class GISDensityMeter extends GISPolygon {

	protected double area;

	public GISDensityMeter(String id, Geometry geometry, double area) {
		super(id, geometry);
		this.area = area;
	}

	@ScheduledMethod(start = 1, interval = 1)
	public double measureDensity() {
		int count = 0;
		ContainsQuery<Object> containsQuery = new ContainsQuery<Object>(this.geography, this.geometry);
		for (Object object : containsQuery.query()) {
			if (object instanceof Student) {
				Student student = (Student) object;
				if (!student.isLearning())
					count++;
			}
		}
		return count / area;
	}

}
