package styles;

import gov.nasa.worldwind.render.SurfacePolygon;
import gov.nasa.worldwind.render.SurfaceShape;
import java.awt.Color;
import gis.GISPolygon;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class GISPolygonStyle implements SurfaceShapeStyle<GISPolygon> {

	/**
	 * Opacity factor
	 */
	public static final double OPACITY_FACTOR = 100.0;

	/**
	 * Get surface shape
	 * 
	 * @param polygon Polygon
	 * @param shape   Shape
	 */
	@Override
	public SurfaceShape getSurfaceShape(GISPolygon polygon,
			SurfaceShape shape) {
		return new SurfacePolygon();
	}

	/**
	 * Get fill color
	 * 
	 * @param polygon Polygon
	 */
	@Override
	public Color getFillColor(GISPolygon polygon) {
		int effectiveContacts = polygon.countEffectiveContacts();
		if (effectiveContacts > 0) {
			return Color.RED;
		} else {
			return Color.WHITE;
		}
	}

	/**
	 * Get fill opacity
	 * 
	 * @param polygon Polygon
	 */
	@Override
	public double getFillOpacity(GISPolygon polygon) {
		int effectiveContacts = polygon.countEffectiveContacts();
		if (effectiveContacts > 0) {
			return Math.min(effectiveContacts / OPACITY_FACTOR, 1.0);
		} else {
			return 0;
		}
	}

	/**
	 * Get line color
	 * 
	 * @param polygon Polygon
	 */
	@Override
	public Color getLineColor(GISPolygon polygon) {
		return Color.BLACK;
	}

	/**
	 * Get line opacity
	 * 
	 * @param polygon Polygon
	 */
	@Override
	public double getLineOpacity(GISPolygon polygon) {
		return 1.0;
	}

	/**
	 * Get line width
	 * 
	 * @param polygon Polygon
	 */
	@Override
	public double getLineWidth(GISPolygon polygon) {
		return 3;
	}

}