package gis;

import com.vividsolutions.jts.geom.Geometry;

public class GISVehicleInOut extends GISDensityMeter {

	public GISVehicleInOut(String id, Geometry geometry, double area, double weight) {
		super(id, geometry, area, weight);
	}

}