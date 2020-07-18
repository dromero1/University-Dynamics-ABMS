package gis;

public class GISDensityMeter extends GISPolygon {

	/**
	 * Area
	 */
	protected double area;

	/**
	 * Active
	 */
	protected boolean active;

	/**
	 * Active
	 */
	protected String link;

	/**
	 * Create a new geo-spatial density meter
	 * 
	 * @param area   Area
	 * @param weight Weight
	 * @param active Active
	 * @param link   Link
	 */
	public GISDensityMeter(double area, double weight, boolean active, String link) {
		super(weight);
		this.area = area;
		this.active = active;
		this.link = link;
	}

	/**
	 * Measure density (instant)
	 */
	public double measureDensity() {
		return this.instantAgentCount / area;
	}

	/**
	 * Measure density (without last departures)
	 */
	public double measureDensityCorrected() {
		return this.arrivals / area;
	}

	/**
	 * Get area
	 */
	public double getArea() {
		return this.area;
	}

	/**
	 * Is active?
	 */
	public boolean isActive() {
		return this.active;
	}

}