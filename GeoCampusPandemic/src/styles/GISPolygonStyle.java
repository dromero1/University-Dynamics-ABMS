package styles;

import java.awt.Color;
import gis.GISPolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class GISPolygonStyle implements SurfaceShapeStyle<GISPolygon> {

	/**
	 * Get surface shape
	 * 
	 * @param obj   GIS polygon
	 * @param shape Surface shape
	 */
	@Override
	public SurfaceShape getSurfaceShape(GISPolygon obj, SurfaceShape shape) {
		return new SurfacePolyline();
	}

	/**
	 * Get line color
	 * 
	 * @param obj GIS polygon
	 */
	@Override
	public Color getLineColor(GISPolygon obj) {
		return Color.BLACK;
	}

	/**
	 * Get line opacity
	 * 
	 * @param obj GIS polygon
	 */
	@Override
	public double getLineOpacity(GISPolygon obj) {
		return 1;
	}

	/**
	 * Get line width
	 * 
	 * @param obj GIS polygon
	 */
	@Override
	public double getLineWidth(GISPolygon obj) {
		return 2;
	}

	/**
	 * Get fill color
	 * 
	 * @param obj GIS polygon
	 */
	@Override
	public Color getFillColor(GISPolygon obj) {
		return Color.WHITE;
	}

	/**
	 * Get fill opacity
	 * 
	 * @param obj GIS polygon
	 */
	@Override
	public double getFillOpacity(GISPolygon obj) {
		return 0.25;
	}

}