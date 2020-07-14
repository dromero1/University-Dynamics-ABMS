package gis;

public class GISTransitArea extends GISDensityMeter {

	/**
	 * Create a new geo-spatial transit area
	 * 
	 * @param area   Area
	 * @param weight Weight
	 * @param active Active
	 * @param link   Link
	 */
	public GISTransitArea(double area, double weight, boolean active, String link) {
		super(area, weight, active, link);
	}

}