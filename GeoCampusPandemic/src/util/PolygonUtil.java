package util;

import java.util.List;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import gis.GISPolygon;
import repast.simphony.gis.util.GeometryUtil;

public final class PolygonUtil {

	/**
	 * Private constructor
	 */
	private PolygonUtil() {
		throw new UnsupportedOperationException("Utility class");
	}

	/**
	 * Get random point from polygon
	 * 
	 * @param polygon Polygon
	 */
	public static Point getRandomPoint(GISPolygon polygon) {
		GeometryFactory geometryFactory = new GeometryFactory();
		Geometry geometry = polygon.getGeometry();
		List<Coordinate> coordinates = GeometryUtil.generateRandomPointsInPolygon(geometry, 1);
		Coordinate coordinate = coordinates.get(0);
		return geometryFactory.createPoint(coordinate);
	}

}