package gis;

import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.space.gis.Geography;

public class GISPolygon {

	/**
	 * Reference to geometry
	 */
	protected Geometry geometry;

	/**
	 * Polygon id
	 */
	protected String id;

	/**
	 * Weight
	 */
	protected double weight;

	/**
	 * Work weight
	 */
	protected double workWeight;

	/**
	 * Reference to geography projection
	 */
	protected Geography<Object> geography;

	/**
	 * Agent count
	 */
	protected int agentCount;

	/**
	 * Departures
	 */
	protected int departures;

	/**
	 * Create a new geo-spatial polygon
	 * 
	 * @param id       Polygon id
	 * @param geometry Reference to geometry
	 * @param weight   Weight
	 */
	public GISPolygon(String id, Geometry geometry, double weight) {
		this.id = id;
		this.geometry = geometry;
		this.weight = weight;
	}

	/**
	 * Create a new geo-spatial polygon
	 * 
	 * @param weight Weight
	 */
	public GISPolygon(double weight) {
		this.weight = weight;
	}

	/**
	 * Set geometry in the geography projection
	 * 
	 * @param geography Reference to geography projection
	 */
	public void setGeometryInGeography(Geography<Object> geography) {
		this.geography = geography;
		this.geography.move(this, this.geometry);
	}

	/**
	 * Handle the 'onArrival' event
	 */
	public void onArrival() {
		this.agentCount++;
	}

	/**
	 * Handle the 'onDeparture' event
	 */
	public void onDeparture() {
		this.departures++;
	}

	/**
	 * Get agent count
	 */
	public int getAgentCount() {
		int count = this.agentCount;
		this.agentCount -= this.departures;
		this.departures = 0;
		return count;
	}

	/**
	 * Set work weight
	 * 
	 * @param weight Work weight
	 */
	public void setWorkWeight(double weight) {
		this.workWeight = weight;
	}

	/**
	 * Get polygon id
	 */
	public String getId() {
		return this.id;
	}

	/**
	 * Set polygon id
	 */
	public void setPolygonId(String id) {
		this.id = id;
	}

	/**
	 * Get reference to geometry
	 */
	public Geometry getGeometry() {
		return this.geometry;
	}

	/**
	 * Set reference to geometry
	 */
	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	/**
	 * Get weight
	 */
	public double getWeight() {
		return this.weight;
	}

	/**
	 * Get work weight
	 */
	public double getWorkWeight() {
		return this.workWeight;
	}

}