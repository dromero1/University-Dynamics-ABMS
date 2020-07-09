package styles;

import gov.nasa.worldwind.render.SurfacePolyline;
import gov.nasa.worldwind.render.SurfaceShape;
import java.awt.Color;
import gis.GISPolygon;
import repast.simphony.visualization.gis3D.style.SurfaceShapeStyle;

public class GISPolygonStyle implements SurfaceShapeStyle<GISPolygon> {

	@Override
	public SurfaceShape getSurfaceShape(GISPolygon object, SurfaceShape shape) {
		return new SurfacePolyline();
	}

	@Override
	public Color getFillColor(GISPolygon obj) {
		return Color.black;
	}

	@Override
	public double getFillOpacity(GISPolygon obj) {
		return 0.25;
	}

	@Override
	public Color getLineColor(GISPolygon obj) {
		return Color.black;
	}

	@Override
	public double getLineOpacity(GISPolygon obj) {
		return 1.0;
	}

	@Override
	public double getLineWidth(GISPolygon obj) {
		return 3;
	}

}