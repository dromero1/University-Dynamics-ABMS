package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISOtherFacility extends GISPolygon {

	/**
	 * Create new geo-spatial generic facility
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 */
	public GISOtherFacility(String id, Geometry geometry) {
		super(id, geometry);
	}

}