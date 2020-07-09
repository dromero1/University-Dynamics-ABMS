package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISInOut extends GISDensityMeter {

	/**
	 * Create a new geo-spatial in-out spot
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 * @param weight   Weight
	 */
	public GISInOut(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}