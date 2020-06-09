package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISEatingPlace extends GISDensityMeter {

	public GISEatingPlace(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}