package gis;

import com.vividsolutions.jts.geom.Geometry;
import model.Student;
import repast.simphony.query.space.gis.ContainsQuery;

public class GISDensityMeter extends GISPolygon implements Comparable<GISDensityMeter> {

	/**
	 * Area
	 */
	protected double area;

	/**
	 * Weight
	 */
	protected double weight;

	/**
	 * Create a new geo-spatial density meter
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 * @param weight   Weight
	 */
	public GISDensityMeter(String id, Geometry geometry, double area, double weight) {
		super(id, geometry);
		this.area = area;
		this.weight = weight;
	}

	/**
	 * Count the number of agents that are currently in the polygon
	 */
	public int countAgents() {
		int count = 0;
		ContainsQuery<Object> containsQuery = new ContainsQuery<Object>(this.geography, this.geometry);
		for (Object object : containsQuery.query()) {
			if (object instanceof Student)
				count++;
		}
		return count;
	}

	/**
	 * Measure the current population density
	 */
	public double measureDensity() {
		int count = countAgents();
		return count / area;
	}

	/**
	 * Compare the current density meter to another one
	 */
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

	/**
	 * Get area
	 */
	public double getArea() {
		return this.area;
	}

	/**
	 * Get weight
	 */
	public double getWeight() {
		return this.weight;
	}

}