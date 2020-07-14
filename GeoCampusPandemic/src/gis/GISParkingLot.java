package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISParkingLot extends GISPolygon {

	/**
	 * Create a new geo-spatial parking lot
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 */
	public GISParkingLot(String id, Geometry geometry) {
		super(id, geometry, 0.0);
	}

}