package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISEatingPlace extends GISDensityMeter {

	public GISEatingPlace(String id, Geometry geometry, double area) {
		super(id, geometry, area);
	}

}