package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISTeachingFacility extends GISDensityMeter {

	/**
	 * Create a new geo-spatial teaching facility
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 * @param weight   Weight
	 */
	public GISTeachingFacility(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}