package gis;

import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.space.gis.Geography;

public abstract class GISPolygon {

	protected Geometry geometry;
	protected String id;
	protected Geography<Object> geography;

	public GISPolygon(String id, Geometry geometry) {
		this.id = id;
		this.geometry = geometry;
	}

	public void setGeometryInGeography(Geography<Object> geography) {
		this.geography = geography;
		this.geography.move(this, geometry);
	}

	public String getId() {
		return id;
	}

	public Geometry getGeometry() {
		return geometry;
	}

}