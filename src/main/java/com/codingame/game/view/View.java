package com.codingame.game.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.game.Player;
import com.codingame.game.build.RandomUtil;
import com.codingame.game.core.Field;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
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
	
	private Text statisticsPlayer1;
	private Text statisticsPlayer2;
		
	private Map<Field, Vector2D> cachedFieldPositions;
	private Map<Region, Integer> cachedRegionColors;
	
	public View(GraphicEntityModule graphicEntityModule) {
		this.graphicEntityModule = graphicEntityModule;
	}
	
	//**********************************************************************
	//*** initial textures
	//**********************************************************************
	
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
				.setWidth(120).setHeight(120) //
				.setX(80).setY(140) //
				.setFillColor(0xffffff);
		graphicEntityModule.createSprite() //
				.setBaseHeight(116).setBaseWidth(116) //
				.setX(140).setY(200).setAnchor(0.5) //
				.setImage(player1.getAvatarToken());
		
		graphicEntityModule.createText(player1.getNicknameToken()).setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(40) //
				.setX(240).setY(80).setAnchor(0.5);
		
		graphicEntityModule.createText("Fields:\nTroops:\nSupply:").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(230).setY(155);
		
		statisticsPlayer1 = graphicEntityModule.createText("9\n42\n501").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(360).setY(155);
		
		// player 2
		graphicEntityModule.createRectangle() //
				.setWidth(140).setHeight(140) //
				.setX(70).setY(400) //
				.setLineWidth(0).setFillColor(player2.getColorToken());
		graphicEntityModule.createRectangle() //
				.setWidth(120).setHeight(120) //
				.setX(80).setY(410) //
				.setLineWidth(0).setFillColor(0xffffff);
		graphicEntityModule.createSprite() //
				.setBaseHeight(116).setBaseWidth(116) //
				.setX(140).setY(470).setAnchor(0.5) //
				.setImage(player2.getAvatarToken());
		
		graphicEntityModule.createText(player2.getNicknameToken()).setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(40) //
				.setX(240).setY(350).setAnchor(0.5);
		
		graphicEntityModule.createText("Fields:\nTroops:\nSupply:").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
				.setX(230).setY(425);
		
		statisticsPlayer2 = graphicEntityModule.createText("9\n42\n501").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(30) //
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
	
	//**********************************************************************
	//*** updated textures
	//**********************************************************************
	
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
	
	public void drawRegions(Set<Region> regions) {
		// for each point, draw point in color of nearest region
		Map<Region, Integer> coloring = getRegionColors(regions);
		Set<Field> fields = regions.stream().flatMap(r -> r.fields.stream()).collect(Collectors.toSet());
		Map<Field, Vector2D> fieldPositions = getPositions(fields);
		
		RegionGrid grid = new RegionGrid(regions, fieldPositions);
		grid.setWidth(GAME_FIELD_WIDTH);
		grid.setHeight(GAME_FIELD_HEIGHT);
		grid.setX(GAME_FIELD_X);
		grid.setY(GAME_FIELD_Y);
		grid.setResolution(10);
		grid.setDistNorm(1);
		Map<Region, Polygon> regionPolys = grid.createRegionPolygons(graphicEntityModule);
		
		for (Region region : regions ) {
			Polygon poly = regionPolys.get(region);
			
			poly.setFillColor(coloring.get(region));
			poly.setAlpha(0.5);
		}
	}
	
	public void drawConnections(Set<Pair<Field, Field>> connections, Set<Region> regions) {
		Set<Field> fields = connections.stream().flatMap(pair -> Stream.of(pair.getKey(), pair.getValue())).collect(Collectors.toSet());
		Map<Field, Vector2D> positions = getPositions(fields);

		for (Pair<Field, Field> connection : connections) {
			Vector2D pos1 = positions.get(connection.getKey());
			Vector2D pos2 = positions.get(connection.getValue());
			if (pos2 == null) {
				System.err.println("Value was: "+connection.getValue().id);
			}
			Vector2D dir = pos2.sub(pos1);
			Vector2D inter1 = findClosestBorderIntersection(pos1, dir);
			Vector2D inter2 = findClosestBorderIntersection(pos2, dir);
			
			if (inter1 == null) {
				System.err.println("inter1 is null: "+connection.getValue().id+" "+connection.getKey().id);
				inter1 = findClosestBorderIntersection(pos1, dir);
			}
			
			if (inter2 == null) {
				System.err.println("inter2 is null: "+connection.getValue().id+" "+connection.getKey().id);
			}
			
			double directLength = pos1.distance(pos2);
			boolean drawIndrect = false;
			// TODO remove if as soon as position bugs are gone...
			if (inter1 != null && inter2 != null) {
				double indirectLength = pos1.distance(inter1) + pos2.distance(inter2);
				drawIndrect = directLength > indirectLength && !getRegion(connection.getKey(), regions).equals(getRegion(connection.getValue(), regions));
			}
			
			if (drawIndrect) {
				graphicEntityModule.createLine().setX((int)pos1.x).setY((int)pos1.y).setX2((int)inter1.x).setY2((int)inter1.y).setLineWidth(3).setLineColor(0x2A914E);
				graphicEntityModule.createLine().setX((int)pos2.x).setY((int)pos2.y).setX2((int)inter2.x).setY2((int)inter2.y).setLineWidth(3).setLineColor(0x2A914E);
			} else {
				graphicEntityModule.createLine().setX((int)pos1.x).setY((int)pos1.y).setX2((int)pos2.x).setY2((int)pos2.y).setLineWidth(3).setLineColor(0x2A914E);
			}
		}
	}
	
	private Region getRegion(Field field, Set<Region> regions) {
		return regions.stream().filter(r -> r.fields.stream().anyMatch(f -> f.equals(field))).findFirst().get();
	}
	
	private Vector2D findClosestBorderIntersection(Vector2D start, Vector2D dir) {
		Vector2D[] borderPoints = {new Vector2D(GAME_FIELD_X, GAME_FIELD_Y), 
				new Vector2D(GAME_FIELD_X+GAME_FIELD_WIDTH, GAME_FIELD_Y),
				new Vector2D(GAME_FIELD_X+GAME_FIELD_WIDTH, GAME_FIELD_Y+GAME_FIELD_HEIGHT),
				new Vector2D(GAME_FIELD_X, GAME_FIELD_Y+GAME_FIELD_HEIGHT)};
		Vector2D closest = null;
		double minDist = Double.MAX_VALUE;
		
		for (int i = 0; i < 4; i++) {
			Vector2D borderA = borderPoints[i];
			Vector2D borderB = borderPoints[(i+1)%4];
			
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
			double t = (start2.y-start1.y)/dir1.y;
			
			return start1.add(dir1.setLength(dir1.length()*t));
		} else {
			// treat dir2.y as 0
			double t = (start2.x-start1.x)/dir1.x;
			
			return start1.add(dir1.setLength(dir1.length()*t));
		}
	}
	
	public void drawFields(Set<Field> fields) {
		Map<Field, Vector2D> positions = getPositions(fields);

		for (Field field : fields) {
			Vector2D pos = positions.get(field);
			
			System.err.format("Drawing field: id=%d, troops=%d, x=%d, y=%d\n", field.id, field.getTroops(), (int)pos.x, (int)pos.y);
			
			graphicEntityModule.createText(field.id+"").setFontSize(40).setX((int)pos.x).setY((int)pos.y);
		}
	}

	private Map<Field, Vector2D> getPositions(Set<Field> fields) {
		if (cachedFieldPositions == null) {
			cachedFieldPositions = estimateFieldPositions(fields);
		}
		
		return cachedFieldPositions;
	}

	private Map<Region, Integer> getRegionColors(Set<Region> regions) {
		if (cachedRegionColors == null) {
			cachedRegionColors = new HashMap<Region, Integer>();
			
			for (Region region : regions) {
				cachedRegionColors.put(region, (1+region.getId().hashCode()*1337)%0xFFFFF);
			}
		} 
		
		return cachedRegionColors;
	}
	
	private Map<Field, Vector2D> estimateFieldPositions(Set<Field> fields) {
		Map<Field, Vector2D> positions = new HashMap<Field, Vector2D>();
		
		// right now, we use hard coded values
		if (fields.size() == 8) { // map_one_region
			double xDist = 0.25*GAME_FIELD_WIDTH;
			double yDist = 0.4*GAME_FIELD_HEIGHT;
			
			for (Field field : fields) {
				int id = field.id;
				boolean mirrorX = id >= fields.size()/2;
				boolean mirrorY = false;
				id = id % (fields.size()/2);
				
				double xDiff = xDist/2;
				double yDiff = yDist;
				
				if (id == 1) {
					xDiff += xDist;
				}
				
				while(id >= 1) {
					yDiff-=yDist;
					id-=2;
				}
				
				if (mirrorX)
					xDiff*=-1;
				
				if (mirrorY)
					yDiff*=-1;
					
				double x = GAME_FIELD_WIDTH/2 - xDiff;
				double y = GAME_FIELD_HEIGHT/2 - yDiff;
				
				positions.put(field, new Vector2D(GAME_FIELD_X+x, GAME_FIELD_Y+y));
			}
		} else if (fields.size() == 18) { // map_two_regions
			double xDist = 0.15*GAME_FIELD_WIDTH;
			double yDist = 0.3*GAME_FIELD_HEIGHT;
			
			for (Field field : fields) {
				int id = field.id;
				boolean mirrorX = id >= fields.size()/2;
				boolean mirrorY = false;
				id = id % (fields.size()/2);
				
				double xDiff = 0;
				double yDiff = 0;
				
				if (id == 2) {
					xDiff = 3 * xDist;
					yDiff = 0;
				} else if (id < 2 || id > 6) {
					int idX = id < 2 ? id : id - 7;
					xDiff = (2-idX) * xDist;
					yDiff = 1.5 * yDist;

					mirrorY = id > 6;
				} else { // 3, 4, 5 6
					int idX = id < 5 ? id : id - 2;
					xDiff = idX == 3 ? 2.25 * xDist : .5 * xDist;
					yDiff = 0.5 * yDist;

					mirrorY = id > 4;					
				}
				
				if (mirrorX)
					xDiff*=-1;
				
				if (mirrorY)
					yDiff*=-1;
					
				double x = GAME_FIELD_WIDTH/2 - xDiff;
				double y = GAME_FIELD_HEIGHT/2 - yDiff;
				
				positions.put(field, new Vector2D(GAME_FIELD_X+x, GAME_FIELD_Y+y));
			}
		} else if (fields.size() == 26) { // map_five_regions
			double xDist = 0.12*GAME_FIELD_WIDTH;
			double yDist = 0.2*GAME_FIELD_HEIGHT;
			
			for (Field field : fields) {
				int id = field.id;
				boolean mirrorX = id >= fields.size()/2;
				boolean mirrorY = false;
				id = id % (fields.size()/2);
				
				double xDiff = 0;
				double yDiff = 0;
				
				if (id >= 10 && id <= 12) { // region C
					xDiff = xDist;
					yDiff = -(id-11) * yDist;
				} else if (id >= 0 && id <= 5) {// region A
					int row = 0;
					double regionMoveX = 0.75;
					
					if (id < 3) {
						xDiff = regionMoveX * (5-id) * xDist;
						row = 0;
					} else if (id < 5) {
						xDiff = regionMoveX * (7.5-id) * xDist;
						row = 1;
					} else {
						xDiff = regionMoveX * (5-1) * xDist;
						row = 2;
					}
					
					yDiff = 0.75 * (3-row) * yDist;
				} else { // region B
					int row = 0;
					double regionMoveX = 0.75;
					
					if (id == 6) {
						xDiff = regionMoveX * (4) * xDist;
						row = 0;
					} else if (id == 9) {
						xDiff = regionMoveX * (4) * xDist;
						row = 2;
					} else {
						xDiff = regionMoveX * (11.5-id) * xDist;
						row = 1;
					}
					
					yDiff = -0.75 * (row+1) * yDist;
				}
					
				if (mirrorX)
					xDiff*=-1;
				
				if (mirrorY)
					yDiff*=-1;
					
				double x = GAME_FIELD_WIDTH/2 - xDiff;
				double y = GAME_FIELD_HEIGHT/2 - yDiff;
				
				positions.put(field, new Vector2D(GAME_FIELD_X+x, GAME_FIELD_Y+y));
			}
		} else {
			// do random placement if map not found (improvement would be to mirror half...)
			for (Field field : fields) {
				double x = RandomUtil.getInstance().nextFloat() * GAME_FIELD_WIDTH;
				double y = RandomUtil.getInstance().nextFloat() * GAME_FIELD_HEIGHT;
				positions.put(field, new Vector2D(GAME_FIELD_X+x, GAME_FIELD_Y+y));
			}
		}
			
		return positions;
	}
}
