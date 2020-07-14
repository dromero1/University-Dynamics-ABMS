package gis;

import com.vividsolutions.jts.geom.Geometry;

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
	 * Measure the current population density
	 */
	public double measureDensity() {
		return this.agentCount / area;
	}

	/**
	 * Get area
	 */
	public double getArea() {
		return this.area;
	}

}