package gis;

import com.vividsolutions.jts.geom.Geometry;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.space.gis.Geography;

public class GISPolygon {

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
	 * Link
	 */
	protected String link;

	/**
	 * Reference to geometry
	 */
	protected Geometry geometry;

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
	 * Effective contacts
	 */
	protected int effectiveContacts;

	/**
	 * Create a new geo-spatial polygon
	 * 
	 * @param weight Weight
	 * @param active Active
	 * @param link   Link
	 */
	public GISPolygon(double weight, boolean active, String link) {
		this.weight = weight;
		this.active = active;
		this.link = link;
	}

	/**
	 * Set geometry in the geography projection
	 * 
	 * @param geography Reference to geography projection
	 * @param geometry  Reference to geometry
	 */
	public void setGeometryInGeography(Geography<Object> geography, Geometry geometry) {
		this.geography = geography;
		this.geometry = geometry;
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
	 * Handle the 'onEffectiveContact' event
	 */
	public void onEffectiveContact() {
		this.effectiveContacts++;
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
	 * Count effective contacts
	 */
	public int countEffectiveContacts() {
		return this.effectiveContacts;
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
	 * 
	 * @param id Polygon id
	 */
	public void setPolygonId(String id) {
		this.id = id;
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

	/**
	 * Get link
	 */
	public String getLink() {
		return this.link;
	}

	/**
	 * Get reference to geometry
	 */
	public Geometry getGeometry() {
		return this.geometry;
	}

}