package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISLimbo extends GISPolygon {

	public static final String LIMBO_ID = "LIM";

	public GISLimbo(Geometry geometry) {
		super(LIMBO_ID, geometry);
	}

}