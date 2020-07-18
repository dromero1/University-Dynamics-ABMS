package styles;

import model.CommunityMember;
import model.DiseaseStage;
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

	private Map<String, WWTexture> textureMap;

	private static final float MAX_SCALE = 2f;

	private static final int WIDTH = 3;

	private static final int HEIGHT = 3;

	public CommunityMemberStyle() {
		Dimension dimension = new Dimension(WIDTH, HEIGHT);
		this.textureMap = new HashMap<String, WWTexture>();
		// Black circle
		BufferedImage image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, MAX_SCALE,
				Color.BLACK);
		this.textureMap.put("black-circle", new BasicWWTexture(image));
		// Orange circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, MAX_SCALE, Color.ORANGE);
		this.textureMap.put("orange-circle", new BasicWWTexture(image));
		// Green circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, MAX_SCALE, Color.GREEN);
		this.textureMap.put("green-circle", new BasicWWTexture(image));
		// Red circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, MAX_SCALE, Color.RED);
		this.textureMap.put("red-circle", new BasicWWTexture(image));
		// Blue circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, MAX_SCALE, Color.BLUE);
		textureMap.put("blue-circle", new BasicWWTexture(image));
		// Gray circle
		image = PatternFactory.createPattern(PatternFactory.PATTERN_CIRCLE, dimension, MAX_SCALE, Color.GRAY);
		this.textureMap.put("gray-circle", new BasicWWTexture(image));
	}

	@Override
	public WWTexture getTexture(CommunityMember communityMember, WWTexture texture) {
		DiseaseStage diseaseStage = communityMember.getDiseaseStage();
		switch (diseaseStage) {
		case DEAD:
			return textureMap.get("black-circle");
		case EXPOSED:
			return textureMap.get("orange-circle");
		case IMMUNE:
			return textureMap.get("green-circle");
		case INFECTED:
			return textureMap.get("red-circle");
		case SUSCEPTIBLE:
			return textureMap.get("blue-circle");
		default:
			return textureMap.get("gray-circle");
		}
	}

	@Override
	public double getScale(CommunityMember communityMember) {
		return MAX_SCALE;
	}

	@Override
	public Offset getIconOffset(CommunityMember communityMember) {
		return Offset.CENTER;
	}

	@Override
	public Offset getLabelOffset(CommunityMember communityMember) {
		return null;
	}

	@Override
	public PlaceMark getPlaceMark(CommunityMember communityMember, PlaceMark mark) {
		if (mark == null) {
			mark = new PlaceMark();
		}
		mark.setAltitudeMode(WorldWind.CLAMP_TO_GROUND);
		mark.setLineEnabled(false);
		return mark;
	}

	@Override
	public double getElevation(CommunityMember communityMember) {
		return 0;
	}

	@Override
	public double getHeading(CommunityMember communityMember) {
		return 0;
	}

	@Override
	public Color getLabelColor(CommunityMember communityMember) {
		return null;
	}

	@Override
	public double getLineWidth(CommunityMember communityMember) {
		return 1;
	}

	@Override
	public Font getLabelFont(CommunityMember communityMember) {
		return null;
	}

	@Override
	public String getLabel(CommunityMember communityMember) {
		return null;
	}

	@Override
	public Material getLineMaterial(CommunityMember communityMember, Material lineMaterial) {
		if (lineMaterial == null) {
			lineMaterial = new Material(Color.RED);
		}
		return lineMaterial;
	}

}