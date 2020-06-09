package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISInOut extends GISDensityMeter {

	public GISInOut(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}