package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISSharedArea extends GISDensityMeter {

	public GISSharedArea(String id, Geometry geometry, double area) {
		super(id, geometry, area);
	}

}