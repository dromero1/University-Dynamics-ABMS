package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISTransitArea extends GISDensityMeter {

	/**
	 * Create a new geo-spatial transit area
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param area     Area
	 */
	public GISTransitArea(String id, Geometry geometry, double area) {
		super(id, geometry, area);
	}

}