package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISParkingLot extends GISPolygon {

	public GISParkingLot(String id, Geometry geometry) {
		super(id, geometry);
	}

}