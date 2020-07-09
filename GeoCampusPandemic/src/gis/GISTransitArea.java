package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISTransitArea extends GISPolygon {

	/**
	 * Create a new geo-spatial transit area
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 */
	public GISTransitArea(String id, Geometry geometry) {
		super(id, geometry);
	}

}