package gis;

public class GISDensityMeter extends GISPolygon {

	/**
	 * Area
	 */
	private double area;

	/**
	 * Create a new geo-spatial density meter
	 * 
	 * @param area   Area
	 * @param weight Weight
	 * @param active Active
	 * @param link   Link
	 */
	public GISDensityMeter(double area, double weight, boolean active,
			String link) {
		super(weight, active, link);
		this.area = area;
	}

	/**
	 * Measure density
	 */
	public double measureDensity() {
		return this.instantAgentCount / this.area;
	}

	/**
	 * Get area
	 */
	public double getArea() {
		return this.area;
	}

}