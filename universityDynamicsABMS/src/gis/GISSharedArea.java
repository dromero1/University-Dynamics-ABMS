package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISSharedArea extends GISDensityMeter {

	/**
	 * Create new geo-spatial shared area
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 * @param weight   Weight
	 */
	public GISSharedArea(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}