package styles;

import model.CommunityMember;
import model.Compartment;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;
import gov.nasa.worldwind.WorldWind;
import gov.nasa.worldwind.render.BasicWWTexture;
import gov.nasa.worldwind.render.Material;
import gov.nasa.worldwind.render.Offset;
import gov.nasa.worldwind.render.PatternFactory;
import gov.nasa.worldwind.render.WWTexture;
import repast.simphony.visualization.gis3D.PlaceMark;
import repast.simphony.visualization.gis3D.style.MarkStyle;

public class CommunityMemberStyle implements MarkStyle<CommunityMember> {

	/**
	 * Standard scale
	 */
	private static final float STANDARD_SCALE = 1.5f;

	/**
	 * Dimension width
	 */
	private static final int WIDTH = 3;

	/**
	 * Dimension height
	 */
	private static final int HEIGHT = 3;

	/**
	 * Texture map
	 */
	private Map<String, WWTexture> textureMap;

	/**
	 * Create a new community member style
	 */
	public CommunityMemberStyle() {
		Dimension dimension = new Dimension(WIDTH, HEIGHT);
		this.textureMap = new HashMap<String, WWTexture>();
		// Black circle
		BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, STANDARD_SCALE,
				Color.BLACK);
		this.textureMap.put("black-circle", new BasicWWTexture(image));
		// Orange circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, STANDARD_SCALE, Color.ORANGE);
		this.textureMap.put("orange-circle", new BasicWWTexture(image));
		// Green circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, STANDARD_SCALE, Color.GREEN);
		this.textureMap.put("green-circle", new BasicWWTexture(image));
		// Red circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, STANDARD_SCALE, Color.RED);
		this.textureMap.put("red-circle", new BasicWWTexture(image));
		// Blue circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, STANDARD_SCALE, Color.BLUE);
		textureMap.put("blue-circle", new BasicWWTexture(image));
		// Gray circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, STANDARD_SCALE, Color.GRAY);
		this.textureMap.put("gray-circle", new BasicWWTexture(image));
	}

	/**
	 * Get texture
	 * 
	 * @param communityMember Community member
	 * @param texture         Texture
	 */
	@Override
	public WWTexture getTexture(CommunityMember communityMember, WWTexture texture) {
		Compartment compartment = communityMember.getCompartment();
		switch (compartment) {
		case DEAD:
			return this.textureMap.get("black-circle");
		case EXPOSED:
			return this.textureMap.get("orange-circle");
		case IMMUNE:
			return this.textureMap.get("green-circle");
		case INFECTED:
			return this.textureMap.get("red-circle");
		case SUSCEPTIBLE:
			return this.textureMap.get("blue-circle");
		default:
			return this.textureMap.get("gray-circle");
		}
	}

	/**
	 * Get scale
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public double getScale(CommunityMember communityMember) {
		Compartment compartment = communityMember.getCompartment();
		switch (compartment) {
		case SUSCEPTIBLE:
		case DEAD:
		case IMMUNE:
			return STANDARD_SCALE;
		case EXPOSED:
		case INFECTED:
			return STANDARD_SCALE * 2;
		default:
			return 0;
		}
	}

	/**
	 * Get icon offset
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public Offset getIconOffset(CommunityMember communityMember) {
		return Offset.CENTER;
	}

	/**
	 * Get label offset
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public Offset getLabelOffset(CommunityMember communityMember) {
		return null;
	}

	/**
	 * Get place mark
	 * 
	 * @param communityMember Community member
	 * @param mark            Place mark
	 */
	@Override
	public PlaceMark getPlaceMark(CommunityMember communityMember, PlaceMark mark) {
		if (mark == null) {
			mark = new PlaceMark();
		}
		mark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		mark.setLineEnabled(false);
		return mark;
	}

	/**
	 * Get elevation
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public double getElevation(CommunityMember communityMember) {
		return 0;
	}

	/**
	 * Get heading
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public double getHeading(CommunityMember communityMember) {
		return 0;
	}

	/**
	 * Get label color
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public Color getLabelColor(CommunityMember communityMember) {
		return null;
	}

	/**
	 * Get line width
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public double getLineWidth(CommunityMember communityMember) {
		return 1;
	}

	/**
	 * Get label font
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public Font getLabelFont(CommunityMember communityMember) {
		return null;
	}

	/**
	 * Get label
	 * 
	 * @param communityMember Community member
	 */
	@Override
	public String getLabel(CommunityMember communityMember) {
		return null;
	}

	/**
	 * Get line material
	 * 
	 * @param communityMember Community member
	 * @param lineMaterial    Line material
	 */
	@Override
	public Material getLineMaterial(CommunityMember communityMember, Material lineMaterial) {
		if (lineMaterial == null) {
			lineMaterial = new Material(Color.RED);
		}
		return lineMaterial;
	}

}