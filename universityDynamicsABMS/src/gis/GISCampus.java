package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISCampus extends GISPolygon {

	public static final String CAMPUS_ID = "CMP";

	public GISCampus(Geometry geometry) {
		super(CAMPUS_ID, geometry);
	}

}