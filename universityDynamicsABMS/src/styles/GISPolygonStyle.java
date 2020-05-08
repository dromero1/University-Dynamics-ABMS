package styles;

import java.awt.Color;
import gis.GISPolygon;
import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceShape;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class GISPolygonStyle implements SurfaceShapeStyle<GISPolygon> {

	@Override
	public SurfaceShape getSurfaceShape(GISPolygon obj, SurfaceShape shape) {
		return new SurfacePolyline();
	}

	@Override
	public Color getLineColor(GISPolygon obj) {
		return Color.BLACK;
	}

	@Override
	public double getLineOpacity(GISPolygon obj) {
		return 1;
	}

	@Override
	public double getLineWidth(GISPolygon obj) {
		return 2;
	}

	@Override
	public Color getFillColor(GISPolygon obj) {
		return Color.WHITE;
	}

	@Override
	public double getFillOpacity(GISPolygon obj) {
		return 0.25;
	}

}