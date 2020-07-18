package gis;

public class GISDensityMeter extends GISPolygon {

	/**
	 * Area
	 */
	protected double area;

	/**
	 * Create a new geo-spatial density meter
	 * 
	 * @param area   Area
	 * @param weight Weight
	 * @param active Active
	 */
	public GISDensityMeter(double area, double weight, boolean active) {
		super(weight, active);
		this.area = area;
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

}