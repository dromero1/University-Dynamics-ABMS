package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISEatingPlace extends GISDensityMeter {

	/**
	 * Create a new geo-spatial eating place
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 * @param weight   Weight
	 */
	public GISEatingPlace(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}