package gis;

import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.space.gis.Geography;

public abstract class GISPolygon {

	protected Geometry geometry;
	protected String id;
	protected Geography<Object> geography;
	protected int relocationCount;

	public GISPolygon(String id, Geometry geometry) {
		this.id = id;
		this.geometry = geometry;
	}

	public void setGeometryInGeography(Geography<Object> geography) {
		this.geography = geography;
		this.geography.move(this, geometry);
	}

	public void onRelocation() {
		this.relocationCount++;
	}

	public int countRelocations() {
		int count = this.relocationCount;
		this.relocationCount = 0;
		return count;
	}

	public String getId() {
		return this.id;
	}

	public Geometry getGeometry() {
		return this.geometry;
	}

}