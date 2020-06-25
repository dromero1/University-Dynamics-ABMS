package gis;

import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.space.gis.Geography;

public abstract class GISPolygon {

	/**
	 * Reference to geometry
	 */
	protected Geometry geometry;

	/**
	 * Polygon id
	 */
	protected String id;

	/**
	 * Reference to geography projection
	 */
	protected Geography<Object> geography;

	/**
	 * Relocations counter
	 */
	protected int relocationCount;

	/**
	 * Create new geo-spatial polygon
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 */
	public GISPolygon(String id, Geometry geometry) {
		this.id = id;
		this.geometry = geometry;
	}

	/**
	 * Set geometry in the geography projection
	 * 
	 * @param geography Reference to geography projection
	 */
	public void setGeometryInGeography(Geography<Object> geography) {
		this.geography = geography;
		this.geography.move(this, geometry);
	}

	/**
	 * Handle the 'onRelocation' event
	 */
	public void onRelocation() {
		this.relocationCount++;
	}

	/**
	 * Count relocations
	 */
	public int countRelocations() {
		int count = this.relocationCount;
		this.relocationCount = 0;
		return count;
	}

	/**
	 * Get polygon id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Get reference to geometry
	 */
	public Geometry getGeometry() {
		return this.geometry;
	}

}