package com.codingame.game.view;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.Player;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.map.GraphPlacement;
import com.codingame.game.view.map.GraphPlacement.Variant;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Polygon;
import com.codingame.gameengine.module.entities.Text;
import com.codingame.gameengine.module.entities.Text.FontWeight;

/**
 * Draws the game state to the view.
 */
public class View {
	
	public static final int FRAME_WIDTH = 1920;
	public static final int FRAME_HEIGHT = 1080;
	
	public static final int GAME_FIELD_X = 620;
	public static final int GAME_FIELD_Y = 120;
	public static final int GAME_FIELD_WIDTH = 1160;
	public static final int GAME_FIELD_HEIGHT = 860;
	
	private GraphicEntityModule graphicEntityModule;
	
	private Map<Field, Vector2D> cachedFieldPositions;
	private Map<Region, Integer> cachedRegionColors;
	
	// fields that has to be kept up to date during the game
	private Map<Field, Text> fieldText;
	private Text statisticsPlayer1;
	private Text statisticsPlayer2;
	
	public View(GraphicEntityModule graphicEntityModule, GameMap map, Map<Field, Vector2D> initialPositions) {
		this.graphicEntityModule = graphicEntityModule;
		
		initialPositions = addFieldOffset(initialPositions);
		cachedFieldPositions = initialPositions;
		Set<PositionedField> positionedFields = calculateFieldPositions(map, initialPositions);
		cachedFieldPositions = positionedFields.stream().collect(Collectors.toMap(PositionedField::getField, PositionedField::pos));
		
		fieldText = new HashMap<Field, Text>();
	}
	
	private Map<Field, Vector2D> addFieldOffset(Map<Field, Vector2D> initialPositions) {
		Vector2D offset = new Vector2D(GAME_FIELD_X + 50, GAME_FIELD_Y + 50); // +50 so the fields are not pushed to the edge completely
		return initialPositions.entrySet().stream() //
				.map(entry -> Pair.of(entry.getKey(), entry.getValue().add(offset))) //
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}
	
	private Set<PositionedField> calculateFieldPositions(GameMap map, Map<Field, Vector2D> initialPositions) {
		// normalise the initial positions (remove the offset to the drawing game field)
		Vector2D offset = new Vector2D(GAME_FIELD_X + 50, GAME_FIELD_Y + 50); // +50 so the fields are not pushed to the edge completely
		initialPositions = initialPositions.entrySet().stream() //
				.map(entry -> Pair.of(entry.getKey(), entry.getValue().sub(offset))) //
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		
		MapGraph graph = new MapGraph(map, initialPositions);
		GraphPlacement<PositionedField> graphPlacement = new GraphPlacement<>(graph);
		
		// configure hyper-parameters of the graph placement algorithm
		graphPlacement.setVariant(Variant.SPRING_EMBEDDER);
		graphPlacement.setBounds(0, 0, GAME_FIELD_WIDTH - 100, GAME_FIELD_HEIGHT - 100);
		graphPlacement.setIdealSpringLength(250);
		graphPlacement.setIdealClusterDistance(200);
		graphPlacement.setIdealNonAdjacentDistance(400);
		graphPlacement.setDelta(1f);
		graphPlacement.setDeltaCooldown(0.99f);
		graphPlacement.setRepulsiveForce(100f);
		graphPlacement.setSpringForce(50f);
		graphPlacement.setClusterForce(50f);
		//graphPlacement.setMaxForceFactor(1000);
		
		Set<PositionedField> positionedFields = graphPlacement.positionFields();
		
		// add the drawing field offset to all fields
		positionedFields.forEach(field -> field.setPosition(field.pos().add(offset)));
		
		return positionedFields;
	}
	
	// **********************************************************************
	// *** initial textures
	// **********************************************************************
	
	public void drawBackground() {
		graphicEntityModule.createSprite().setImage("background.png") //
				.setBaseWidth(FRAME_WIDTH).setBaseHeight(FRAME_HEIGHT);
		
		graphicEntityModule.createSprite().setImage("background_graph.png") //
				.setX(500).setY(30) //
				.setBaseWidth(1400).setBaseHeight(1040);
		graphicEntityModule.createSprite().setImage("background_player.png") //
				.setX(50).setY(50) //
				.setBaseWidth(400).setBaseHeight(250);
		graphicEntityModule.createSprite().setImage("background_player.png") //
				.setX(50).setY(320) //
				.setBaseWidth(400).setBaseHeight(250);
		graphicEntityModule.createSprite().setImage("background_legend.png") //
				.setX(50).setY(600) //
				.setBaseWidth(400).setBaseHeight(450);
		
		graphicEntityModule.createSprite().setImage("background_banner.png") //
				.setX(25).setY(40) //
				.setBaseWidth(450).setBaseHeight(70);
		graphicEntityModule.createSprite().setImage("background_banner.png") //
				.setX(25).setY(310) //
				.setBaseWidth(450).setBaseHeight(70);
		graphicEntityModule.createSprite().setImage("background_banner.png") //
				.setX(25).setY(580) //
				.setBaseWidth(450).setBaseHeight(70);
	}
	
	public void drawPlayerInfos(Player player1, Player player2) {
		// player 1
		graphicEntityModule.createRectangle() //
				.setWidth(140).setHeight(140) //
				.setX(70).setY(130) //
				.setFillColor(player1.getColorToken());
		graphicEntityModule.createRectangle() //
				.setWidth(116).setHeight(116) //
				.setX(82).setY(142) //
				.setFillColor(0xffffff);
		graphicEntityModule.createSprite() //
				.setBaseHeight(116).setBaseWidth(116) //
				.setX(140).setY(200).setAnchor(0.5) //
				.setImage(player1.getAvatarToken());
		
		graphicEntityModule.createText(player1.getNicknameToken()).setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(40) //
				.setX(240).setY(80).setAnchor(0.5);
		
		graphicEntityModule.createText("Fields:\nTroops:\nSupply:").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(230).setY(155);
		
		statisticsPlayer1 = graphicEntityModule.createText("0\n0\n0").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(360).setY(155);
		
		// player 2
		graphicEntityModule.createRectangle() //
				.setWidth(140).setHeight(140) //
				.setX(70).setY(400) //
				.setLineWidth(0).setFillColor(player2.getColorToken());
		graphicEntityModule.createRectangle() //
				.setWidth(116).setHeight(116) //
				.setX(82).setY(412) //
				.setLineWidth(0).setFillColor(0xffffff);
		graphicEntityModule.createSprite() //
				.setBaseHeight(116).setBaseWidth(116) //
				.setX(140).setY(470).setAnchor(0.5) //
				.setImage(player2.getAvatarToken());
		
		graphicEntityModule.createText(player2.getNicknameToken()).setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(40) //
				.setX(240).setY(350).setAnchor(0.5);
		
		graphicEntityModule.createText("Fields:\nTroops:\nSupply:").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(230).setY(425);
		
		statisticsPlayer2 = graphicEntityModule.createText("0\n0\n0").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(360).setY(425);
	}
	
	public void drawLegend(Set<Region> regions) {
		graphicEntityModule.createText("Regions").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(40) //
				.setX(240).setY(620).setZIndex(20).setAnchor(0.5);
		
		String regionNames = regions.stream().map(Region::getId).map(id -> id + ":").collect(Collectors.joining("\n"));
		String regionNumFields = regions.stream().map(region -> Integer.toString(region.fields.size()) + "F -> ").collect(Collectors.joining("\n"));
		String regionBonuses = regions.stream().map(region -> Integer.toString(region.getBonusTroops()) + "T").collect(Collectors.joining("\n"));
		
		graphicEntityModule.createText(regionNames).setFontFamily("courier").setFontSize(40) //
				.setX(80).setY(680);
		graphicEntityModule.createText(regionNumFields).setFontFamily("courier").setFontSize(40) //
				.setX(200).setY(680);
		graphicEntityModule.createText(regionBonuses).setFontFamily("courier").setFontSize(40) //
				.setX(340).setY(680);
	}
	
	public void drawRegions(Set<Region> regions) {
		// for each point, draw point in color of nearest region
		Map<Region, Integer> coloring = getRegionColors(regions);
		Map<Region, Polygon> regionPolys = getRegionPolynoms(regions);
		
		for (Region region : regions) {
			Polygon poly = regionPolys.get(region);
			
			poly.setFillColor(coloring.get(region));
			poly.setAlpha(0.5);
		}
	}
	
	public void drawConnections(Set<Pair<Field, Field>> connections, Set<Region> regions) {
		Map<Field, Vector2D> positions = cachedFieldPositions;
		
		for (Pair<Field, Field> connection : connections) {
			Vector2D pos1 = positions.get(connection.getKey());
			Vector2D pos2 = positions.get(connection.getValue());
			
			Vector2D dir = pos2.sub(pos1);
			// TODO this border algorithm is not always correct (e.g. going out on the left
			// border and coming back from the top)
			Vector2D inter1 = findClosestBorderIntersection(pos1, dir);
			Vector2D inter2 = findClosestBorderIntersection(pos2, dir);
			
			if (inter1 != null && inter2 != null) { // TODO the border intersection vectors are not always found when using the graph placement algorithm
				double directLength = pos1.distance(pos2);
				double indirectLength = pos1.distance(inter1) + pos2.distance(inter2);
				boolean sameRegion = getRegion(connection.getKey(), regions).equals(getRegion(connection.getValue(), regions));
				boolean drawIndirect = directLength > indirectLength && !sameRegion;
				
				if (drawIndirect) {
					drawConnectionLine(pos1, inter1);
					drawConnectionLine(pos2, inter2);
				}
				else {
					drawConnectionLine(pos1, pos2);
				}
			}
		}
	}
	
	private void drawConnectionLine(Vector2D pos1, Vector2D pos2) {
		graphicEntityModule.createLine() //
				.setX((int) pos1.x).setY((int) pos1.y) //
				.setX2((int) pos2.x).setY2((int) pos2.y) //
				.setLineWidth(3) //
				.setLineColor(0x2A914E);
	}
	
	public void drawFields(Set<Field> fields) {
		Map<Field, Vector2D> positions = cachedFieldPositions;
		int fontSize = 40;
		int fontSizeId = 20;
		
		// get box size for single digit
		Rectangle2D txtBox = getBounds("2", fontSize);
		int xoff = (int) (txtBox.getWidth() / 2);
		int yoff = (int) (txtBox.getHeight() / 2);
		
		fieldText.clear();
		for (Field field : fields) {
			Vector2D pos = positions.get(field);
			
			// draw field box/background
			graphicEntityModule.createRoundedRectangle() //
					.setX((int) pos.x - 5 * xoff / 2) //
					.setY((int) pos.y - 3 * yoff / 2) //
					.setWidth(5 * xoff) //
					.setHeight(3 * yoff) //
					.setFillColor(0x000000);
			
			// add field id to box
			graphicEntityModule.createText(Integer.toString(field.id)) //
					.setFontSize(fontSizeId).setX((int) pos.x) //
					.setY((int) pos.y + 14) //
					.setAnchorX(0.5) //
					.setFillColor(0xffe511);
			
			// add number of troops
			Text cgText = graphicEntityModule.createText() //
					.setFontSize(fontSize) //
					.setFontWeight(FontWeight.BOLD) //
					.setX((int) pos.x) //
					.setY((int) pos.y) //
					.setAnchor(0.5);
			
			// store fields, so we can update them later
			fieldText.put(field, cgText);
		}
		
		// add per frame content
		updateFields(fields);
	}
	
	// **********************************************************************
	// *** updated textures
	// **********************************************************************
	
	public void updatePlayerStats(Owner player, int fields, int troops, int deployable) {
		String statsText = fields + "\n" + troops + "\n" + deployable;
		
		if (player == Owner.PLAYER_1) {
			statisticsPlayer1.setText(statsText);
		}
		else if (player == Owner.PLAYER_2) {
			statisticsPlayer2.setText(statsText);
		}
		else {
			throw new IllegalArgumentException("The parameter 'player' should be PLAYER_1 or PLAYER_2 but was: " + player);
		}
	}
	
	public void updateFields(Set<Field> fields) {
		Map<Owner, Integer> ownerColors = getColorForOwner();
		
		for (Field field : fields) {
			String txt = Integer.toString(field.getTroops());
			
			Text cgText = fieldText.get(field);
			cgText.setText(txt).setFillColor(ownerColors.get(field.getOwner()), Curve.NONE);
		}
	}
	
	// **********************************************************************
	// *** get/estimate resources
	// **********************************************************************
	
	private Region getRegion(Field field, Set<Region> regions) {
		return regions.stream().filter(r -> r.fields.stream().anyMatch(f -> f.equals(field))).findFirst().get();
	}
	
	private Rectangle2D getBounds(String txt, int fontSize) {
		Font font = new Font(txt, Font.PLAIN, 40);
		FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
		return font.getStringBounds(txt, frc);
	}
	
	private Map<Owner, Integer> getColorForOwner() {
		Map<Owner, Integer> colors = new HashMap<Owner, Integer>();
		
		colors.put(Owner.NEUTRAL, 0xd8a629);
		colors.put(Owner.PLAYER_2, 0x636bff);
		colors.put(Owner.PLAYER_1, 0xd51313);
		
		return colors;
	}
	
	private Map<Region, Integer> getRegionColors(Set<Region> regions) {
		if (cachedRegionColors == null) {
			cachedRegionColors = new HashMap<Region, Integer>();
			
			for (Region region : regions) {
				cachedRegionColors.put(region, (1 + region.getId().hashCode() * 1337) % 0xFFFFF);
			}
		}
		
		return cachedRegionColors;
	}
	
	private Map<Region, Polygon> getRegionPolynoms(Set<Region> regions) {
		Map<Field, Vector2D> fieldPositions = cachedFieldPositions;
		
		RegionGrid grid = new RegionGrid(regions, fieldPositions);
		grid.setWidth(GAME_FIELD_WIDTH);
		grid.setHeight(GAME_FIELD_HEIGHT);
		grid.setX(GAME_FIELD_X);
		grid.setY(GAME_FIELD_Y);
		grid.setResolution(8);
		grid.setDistNorm(1);
		
		return grid.createRegionPolygons(graphicEntityModule);
	}
	
	private Vector2D findClosestBorderIntersection(Vector2D start, Vector2D dir) {
		Vector2D[] borderPoints = {new Vector2D(GAME_FIELD_X, GAME_FIELD_Y), new Vector2D(GAME_FIELD_X + GAME_FIELD_WIDTH, GAME_FIELD_Y), new Vector2D(GAME_FIELD_X + GAME_FIELD_WIDTH, GAME_FIELD_Y + GAME_FIELD_HEIGHT), new Vector2D(GAME_FIELD_X, GAME_FIELD_Y + GAME_FIELD_HEIGHT)};
		Vector2D closest = null;
		double minDist = Double.MAX_VALUE;
		
		for (int i = 0; i < 4; i++) {
			Vector2D borderA = borderPoints[i];
			Vector2D borderB = borderPoints[(i + 1) % 4];
			
			Vector2D intersection = findIntersection1D(start, dir, borderA, borderB.sub(borderA));
			
			if (intersection == null)
				continue;
			
			double dist = start.distance(intersection);
			
			if (dist <= minDist) {
				minDist = dist;
				closest = intersection;
			}
		}
		
		return closest;
	}
	
	private Vector2D findIntersection1D(Vector2D start1, Vector2D dir1, Vector2D start2, Vector2D dir2) {
		if (dir1.isLinearlyDependent(dir2))
			return null;
		
		if (Math.abs(dir2.x) <= Math.abs(dir2.y)) {
			// treat dir2.x as 0
			double t = (start2.y - start1.y) / dir1.y;
			
			return start1.add(dir1.setLength(dir1.length() * t));
		}
		else {
			// treat dir2.y as 0
			double t = (start2.x - start1.x) / dir1.x;
			
			return start1.add(dir1.setLength(dir1.length() * t));
		}
	}
}
