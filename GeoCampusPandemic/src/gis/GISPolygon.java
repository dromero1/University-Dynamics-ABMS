package gis;

import com.vividsolutions.jts.geom.Geometry;

import repast.simphony.engine.schedule.ScheduledMethod;
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
	 * Active
	 */
	protected boolean active;

	/**
	 * Reference to geography projection
	 */
	protected Geography<Object> geography;

	/**
	 * Instant agent count
	 */
	protected int instantAgentCount;

	/**
	 * Arrivals
	 */
	protected int arrivals;

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
	 * @param active   Active
	 */
	public GISPolygon(String id, Geometry geometry, double weight, boolean active) {
		this.id = id;
		this.geometry = geometry;
		this.weight = weight;
		this.active = active;
	}

	/**
	 * Create a new geo-spatial polygon
	 * 
	 * @param weight Weight
	 * @param active Active
	 */
	public GISPolygon(double weight, boolean active) {
		this.weight = weight;
		this.active = active;
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
		this.instantAgentCount++;
		this.arrivals++;
	}

	/**
	 * Handle the 'onDeparture' event
	 */
	public void onDeparture() {
		this.instantAgentCount--;
		this.departures++;
	}

	/**
	 * Count agents (instant)
	 */
	public int countAgents() {
		return this.instantAgentCount;
	}

	/**
	 * Count agents (without last departures)
	 */
	public int countAgentsCorrected() {
		return this.arrivals;
	}

	/**
	 * Reset departures
	 */
	@ScheduledMethod(start = 0, interval = 0.375)
	public void resetDepartures() {
		this.arrivals -= this.departures;
		this.departures = 0;
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

	/**
	 * Is active?
	 */
	public boolean isActive() {
		return this.active;
	}

}