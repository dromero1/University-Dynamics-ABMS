package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISCampus extends GISPolygon {

	/**
	 * Campus' id
	 */
	public static final String CAMPUS_ID = "CMP";

	/**
	 * Create a new geo-spatial campus
	 * 
	 * @param geometry Reference to geometry
	 */
	public GISCampus(Geometry geometry) {
		super(CAMPUS_ID, geometry, 0.0, true);
	}

}