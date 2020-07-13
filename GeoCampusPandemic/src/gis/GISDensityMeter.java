package gis;

import com.vividsolutions.jts.geom.Geometry;
import model.CommunityMember;
import repast.simphony.query.space.gis.ContainsQuery;

public class GISDensityMeter extends GISPolygon {

	/**
	 * Area
	 */
	protected double area;

	/**
	 * Create a new geo-spatial density meter
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 * @param weight   Weight
	 */
	public GISDensityMeter(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, weight);
		this.area = area;
	}

	/**
	 * Create a new geo-spatial density meter
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 */
	public GISDensityMeter(String id, Geometry geometry, double area) {
		super(id, geometry);
		this.area = area;
	}

	/**
	 * Count the number of agents that are currently in the polygon
	 */
	public int countAgents() {
		int count = 0;
		ContainsQuery<Object> containsQuery = new ContainsQuery<Object>(this.geography, this.geometry);
		for (Object object : containsQuery.query()) {
			if (object instanceof CommunityMember)
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
	 * Get area
	 */
	public double getArea() {
		return this.area;
	}

}