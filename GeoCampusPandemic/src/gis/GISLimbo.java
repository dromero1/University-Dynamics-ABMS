package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISLimbo extends GISPolygon {

	/**
	 * Create a new geo-spatial limbo
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 */
	public GISLimbo(String id, Geometry geometry) {
		super(id, geometry);
	}

}