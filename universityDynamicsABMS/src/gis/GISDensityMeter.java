package gis;

import com.vividsolutions.jts.geom.Geometry;
import model.Student;
import repast.simphony.query.space.gis.ContainsQuery;

public class GISDensityMeter extends GISPolygon implements Comparable<GISDensityMeter> {

	protected double area;
	protected double weight;

	public GISDensityMeter(String id, Geometry geometry, double area, double weight) {
		super(id, geometry);
		this.area = area;
		this.weight = weight;
	}

	public double countAgents() {
		int count = 0;
		ContainsQuery<Object> containsQuery = new ContainsQuery<Object>(this.geography, this.geometry);
		for (Object object : containsQuery.query()) {
			if (object instanceof Student) {
				count++;
			}
		}
		return count;
	}

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

	@Override
	public int compareTo(GISDensityMeter densityMeter) {
		if (weight > densityMeter.getWeight()) {
			return 1;
		} else if (weight < densityMeter.getWeight()) {
			return -1;
		} else {
			return 0;
		}
	}

	public double getArea() {
		return area;
	}

	public double getWeight() {
		return weight;
	}

}