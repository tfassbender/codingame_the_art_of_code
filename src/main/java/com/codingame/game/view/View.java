package com.codingame.game.view;

import java.awt.Font;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.codingame.game.Action;
import com.codingame.game.Player;
import com.codingame.game.build.RandomUtil;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.core.TurnType;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.MovementEvents.MovementStep;
import com.codingame.game.view.map.FieldMinDistancePlacement;
import com.codingame.game.view.map.GraphPlacement;
import com.codingame.game.view.map.GraphPlacement.Variant;
import com.codingame.gameengine.module.entities.Circle;
import com.codingame.gameengine.module.entities.Curve;
import com.codingame.gameengine.module.entities.Entity;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Polygon;
import com.codingame.gameengine.module.entities.Sprite;
import com.codingame.gameengine.module.entities.SpriteAnimation;
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
	
	private static final int TEXT_TROOPS_Y_OFFSET = 25;
	
	private GraphicEntityModule graphicEntityModule;
	
	private Map<Field, Vector2D> cachedFieldPositions;
	private Map<Region, Integer> cachedRegionColors;
	private String[] gunner_left_right_red;
	private String[] gunner_left_right_blue;
	private Map<Owner, Sprite> pickGraphics;
	private TroopNavigator troopNavi;
	private int colorPlayer1;
	private int colorPlayer2;
	
	// fields that has to be kept up to date during the game
	private Map<Field, Text> fieldText;
	private Map<Field, Text> deployText;
	private Map<Pair<Field, Field>, Pair<SpriteAnimation, SpriteAnimation>> moveAnimations;
	private Map<Pair<Field, Field>, Text> moveText;
	private Map<Region, Pair<Text, Text>> regionLegendTexts;
	private Text statisticsPlayer1;
	private Text statisticsPlayer2;
	
	public View(GraphicEntityModule graphicEntityModule, GameMap map, Map<Field, Vector2D> initialPositions) {
		this.graphicEntityModule = graphicEntityModule;
		
		initialPositions = addFieldOffset(initialPositions);
		cachedFieldPositions = initialPositions;
		Set<PositionedField> positionedFields = calculateFieldPositions(map, initialPositions);
		cachedFieldPositions = positionedFields.stream().collect(Collectors.toMap(PositionedField::getField, PositionedField::pos));
		
		fieldText = new HashMap<Field, Text>();
		deployText = new HashMap<Field, Text>();
		moveAnimations = new HashMap<Pair<Field, Field>, Pair<SpriteAnimation, SpriteAnimation>>();
		moveText = new HashMap<Pair<Field, Field>, Text>();
		pickGraphics = new HashMap<Owner, Sprite>();
		troopNavi = new TroopNavigator();
		
		gunner_left_right_blue = graphicEntityModule.createSpriteSheetSplitter().setName("Gunner_Blue").setOrigCol(0).setOrigRow(0).setImageCount(6).setImagesPerRow(6).setHeight(48).setWidth(48).setSourceImage("Gunner_Blue_Run.png").split();
		gunner_left_right_red = graphicEntityModule.createSpriteSheetSplitter().setName("Gunner_Red").setOrigCol(0).setOrigRow(0).setImageCount(6).setImagesPerRow(6).setHeight(48).setWidth(48).setSourceImage("Gunner_Red_Run.png").split();
	}
	
	private Map<Field, Vector2D> addFieldOffset(Map<Field, Vector2D> initialPositions) {
		Vector2D offset = new Vector2D(GAME_FIELD_X + 50, GAME_FIELD_Y + 50); // +50 so the fields are not pushed to the edge completely
		return initialPositions.entrySet().stream() //
				.map(entry -> Pair.of(entry.getKey(), entry.getValue().add(offset))) //
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
	}
	
	private Set<PositionedField> calculateFieldPositions(GameMap map, Map<Field, Vector2D> initialPositions) {
		// normalize the initial positions (remove the offset to the drawing game field)
		Vector2D offset = new Vector2D(GAME_FIELD_X + 50, GAME_FIELD_Y + 50); // +50 so the fields are not pushed to the edge completely
		initialPositions = initialPositions.entrySet().stream() //
				.map(entry -> Pair.of(entry.getKey(), entry.getValue().sub(offset))) //
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		
		MapGraph graph = new MapGraph(map, initialPositions);
		GraphPlacement<PositionedField> graphPlacement = new GraphPlacement<>(graph);
		
		// configure hyper-parameters of the graph placement algorithm
		graphPlacement.setVariant(Variant.SPRING_EMBEDDER);
		graphPlacement.setBounds(0, 0, GAME_FIELD_WIDTH - 100, GAME_FIELD_HEIGHT - 100);
		graphPlacement.setIterations(100);
		graphPlacement.setIdealSpringLength(350);
		graphPlacement.setIdealClusterDistance(300);
		graphPlacement.setIdealNonAdjacentDistance(1000);
		graphPlacement.setDelta(1f);
		graphPlacement.setDeltaCooldown(0.95f);
		graphPlacement.setRepulsiveForce(500f);
		graphPlacement.setSpringForce(50f);
		graphPlacement.setClusterForce(50f);
		//graphPlacement.setMaxForceFactor(1000);
		
		Set<PositionedField> positionedFields = graphPlacement.positionFields();
		positionedFields = FieldMinDistancePlacement.positionFields(positionedFields);
		
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
		
		colorPlayer1 = player1.getColorToken();
		colorPlayer2 = player2.getColorToken();
	}
	
	public void drawLegend(Set<Region> regions) {
		regionLegendTexts = new HashMap<>();
		Map<Region, Integer> regionColors = getRegionColors(regions);
		// sort regions with more bonus troops to the top of the list
		List<Region> sortedRegions = regions.stream().sorted(Comparator.comparing(Region::getBonusTroops).reversed()).collect(Collectors.toList());
		
		graphicEntityModule.createText("Regions").setFontFamily("courier").setFontWeight(FontWeight.BOLD).setFontSize(40) //
				.setX(240).setY(620).setZIndex(20).setAnchor(0.5);
		
		for (int i = 0; i < sortedRegions.size(); i++) {
			Region region = sortedRegions.get(i);
			Integer color = regionColors.get(region);
			graphicEntityModule.createCircle().setFillColor(color).setRadius(13).setX(130).setY(700 + i * 39);
			Text numFields = graphicEntityModule.createText(Integer.toString(region.fields.size()) + "F -> ").setFontFamily("courier") //
					.setFontSize(40) //
					.setX(170).setY(680 + i * 39);
			Text bonus = graphicEntityModule.createText(Integer.toString(region.getBonusTroops()) + "T").setFontFamily("courier") //
					.setFontSize(40) //
					.setX(310).setY(680 + i * 39);
			
			regionLegendTexts.put(region, Pair.of(numFields, bonus));
		}
		
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
		Set<Field> fields = connections.stream().flatMap(pair -> Stream.of(pair.getKey(), pair.getValue())).collect(Collectors.toSet());
		Map<Field, Vector2D> positions = getPositions(fields);
		
		for (Pair<Field, Field> connection : connections) {
			Vector2D pos1 = positions.get(connection.getKey());
			Vector2D pos2 = positions.get(connection.getValue());
			
			ConnectionFinder connectionFinder = new ConnectionFinder(GAME_FIELD_WIDTH, GAME_FIELD_HEIGHT, GAME_FIELD_X, GAME_FIELD_Y, pos1, pos2);
			Vector2D inter1 = connectionFinder.getIntersection1();
			Vector2D inter2 = connectionFinder.getIntersection2();
			boolean drawDirect = connectionFinder.isDirect();
			
			if (drawDirect) {
				troopNavi.addOnePartConnection(connection, pos1, pos2);
				
				drawConnectionLine(pos1, pos2);
			}
			else {
				troopNavi.addTwoPartConnection(connection, pos1, inter1, inter2, pos2);
				
				drawConnectionLine(pos1, inter1);
				drawConnectionLine(pos2, inter2);
			}
			
			createMovementAnimationEntity(connection, pos1, pos2, connectionFinder.shouldTroopsFaceRight());
			createMovementAnimationEntity(connection.swapped(), pos2, pos1, !connectionFinder.shouldTroopsFaceRight());
		}
	}
	
	private void createMovementAnimationEntity(Pair<Field, Field> connection, Vector2D pos1, Vector2D pos2, boolean faceRight) {
		// troops animations
		SpriteAnimation animationP1 = graphicEntityModule.createSpriteAnimation().setImages(gunner_left_right_red).setX((int) pos1.x).setY((int) pos1.y).setLoop(true).setAnchor(0.5).setScaleX(faceRight ? 1 : -1);
		SpriteAnimation animationP2 = graphicEntityModule.createSpriteAnimation().setImages(gunner_left_right_blue).setX((int) pos1.x).setY((int) pos1.y).setLoop(true).setAnchor(0.5).setScaleX(faceRight ? 1 : -1);
		animationP1.setDuration(3000);
		animationP2.setDuration(3000);
		moveAnimations.put(connection, Pair.of(animationP1, animationP2));
		
		// troop text
		Text text = graphicEntityModule.createText();
		text.setAnchor(0.5);
		text.setFontSize(20);
		moveText.put(connection, text);
	}
	
	private void drawConnectionLine(Vector2D pos1, Vector2D pos2) {
		graphicEntityModule.createLine() //
				.setX((int) pos1.x).setY((int) pos1.y) //
				.setX2((int) pos2.x).setY2((int) pos2.y) //
				.setLineWidth(3) //
				.setLineColor(0x2A914E);
	}
	
	public void drawFields(Set<Field> fields) {
		Map<Field, Vector2D> positions = getPositions(fields);
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
			
			Text deployAt = graphicEntityModule.createText() //
					.setFontSize(fontSize) //
					.setFontWeight(FontWeight.BOLD) //
					.setX((int) pos.x) //
					.setY((int) pos.y - 60) //
					.setAnchor(0.5) //
					.setFillColor(0x32ff4e) //
					.setAlpha(0);
			
			// store fields, so we can update them later
			fieldText.put(field, cgText);
			deployText.put(field, deployAt);
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
	
	public void updateRegionLegend(Set<Region> regions) {
		for (Region region : regions) {
			int color = 0;//black
			if (region.isConqueredBy(Owner.PLAYER_1)) {
				color = colorPlayer1;
			}
			else if (region.isConqueredBy(Owner.PLAYER_2)) {
				color = colorPlayer2;
			}
			Pair<Text, Text> texts = regionLegendTexts.get(region);
			texts.getKey().setFillColor(color, Curve.NONE);
			texts.getValue().setFillColor(color, Curve.NONE);
		}
	}
	
	// **********************************************************************
	// *** animations
	// **********************************************************************
	
	public void animatePicks(Set<Field> fields, Set<PickEvent> picksPerformed) {
		Map<Field, Vector2D> positions = getPositions(fields);
		
		// moved pick creation to a later point to manipulate image order...
		if (pickGraphics.size() == 0) {
			pickGraphics.put(Owner.PLAYER_1, graphicEntityModule.createSprite().setImage("pointing_finger.png").setTint(getColorForOwner().get(Owner.PLAYER_1)).setAlpha(1).setAnchor(0.5).setBaseWidth(100).setBaseHeight(100));
			pickGraphics.put(Owner.PLAYER_2, graphicEntityModule.createSprite().setImage("pointing_finger.png").setTint(getColorForOwner().get(Owner.PLAYER_2)).setAlpha(1).setAnchor(0.5).setBaseWidth(100).setBaseHeight(100));
		}
		
		for (PickEvent pick : picksPerformed) {
			Field field = fields.stream().filter(f -> f.id == pick.targetId).findFirst().get();
			Vector2D position = positions.get(field);
			Sprite hand = pickGraphics.get(pick.owner);
			
			double xOffset = 0;
			double rotation = 0;
			double scale = 1;
			
			// hand is slightly rotated and reduced in size if denied
			// rotation and offset is based mirrored for PLAYER_1 <-> PLAYER_2
			if (pick.denied) {
				xOffset = 80 * (pick.owner == Owner.PLAYER_1 ? -1 : 1);
				rotation = Math.PI / 5. * (pick.owner == Owner.PLAYER_1 ? -1 : 1);
				scale = 0.8;
			}
			
			hand.setX((int) (position.x + xOffset), Curve.NONE).setY((int) position.y - 100, Curve.NONE).setAlpha(1, Curve.NONE) //
					.setScale(scale, Curve.NONE).setRotation(rotation, Curve.NONE);
			graphicEntityModule.commitEntityState(0.2, hand);
		}
	}
	
	public void animateDeployments(List<Action> actions1, List<Action> actions2) {
		List<Action> actions = new ArrayList<Action>();
		
		actions.addAll(actions1);
		actions.addAll(actions2);
		
		for (Field field : deployText.keySet()) {
			// collect deployed troops
			int deployedTroops = actions.stream().filter(a -> a.getTargetId() == field.id && a.getType() == Action.Type.DEPLOY).mapToInt(a -> a.getNumTroops()).sum();
			
			// any deployments at field?
			if (deployedTroops > 0) {
				Text deployAt = deployText.get(field);
				deployAt.setText("+" + deployedTroops);
				graphicEntityModule.commitEntityState(0, deployAt);
				deployAt.setAlpha(1, Curve.EASE_OUT);
				graphicEntityModule.commitEntityState(1, deployAt);
			}
		}
	}
	
	public void animateMovements(MovementEvents events, Set<Field> fields) {
		Set<Pair<Field, Field>> keys = events.getKeys();
		double relativeFightPosition = 0.4; // 40%:= position on a track [0; 1], where troops stand still to shoot
		
		for (Pair<Field, Field> key : keys) {
			Field f1 = key.getKey();
			Field f2 = key.getValue();
			
			// collect all Entities, that are required for this animation
			List<MovementStep> steps = events.getSteps(f1, f2);
			Pair<SpriteAnimation, SpriteAnimation> spriteSelection = moveAnimations.get(key);
			SpriteAnimation troops = steps.get(0).owner == Owner.PLAYER_1 ? spriteSelection.getKey() : spriteSelection.getValue();
			Route route = troopNavi.getRoute(key);
			Text troopText = moveText.get(key);
			
			// time info
			double stepDuration = 1. / steps.size();
			double t = 0;
			
			// capture troops and troopText initial state
			troops.setAlpha(1).reset();
			troopText.setText(steps.get(0).troops + "").setAlpha(1);
			graphicEntityModule.commitEntityState(t, troops, troopText);
			
			for (MovementStep step : steps) {
				List<Pair<Double, Vector2D>> timedPositioning = null;
				boolean lastStep = step == steps.get(steps.size() - 1);
				double tStart = t;
				t += stepDuration;
				
				switch (step.type) {
					case DIE:
						// let troops and text fade away
						troops.setAlpha(0);
						troopText.setAlpha(0);
						break;
					case FIGHT:
						// do not move troops
						
						// add shooting animation
						ConnectionFinder estimatedConnection = null;
						Vector2D shootFrom = route.estimatePosition(0.4);
						Vector2D shootAt = troopNavi.getRoute(step.fightWithMovement).estimatePosition(0.4);
						boolean makeDirectBullet = true;
						
						// create bullet (consider caching/reusing bullets, if we have to much graphical data)
						Circle bullet = graphicEntityModule.createCircle();
						double bulletStartTime = t - stepDuration;
						bullet.setX((int) shootFrom.x).setY((int) shootFrom.y).setRadius(5).setFillColor(0xFFF000);
						graphicEntityModule.commitEntityState(t - stepDuration, bullet);
						
						// if one of the troops paths is indirect, consider indirect bullets
						if (!route.isTrivialRoute() || !troopNavi.getRoute(step.fightWithMovement).isTrivialRoute()) {
							estimatedConnection = new ConnectionFinder(GAME_FIELD_WIDTH, GAME_FIELD_HEIGHT, GAME_FIELD_X, GAME_FIELD_Y, shootFrom, shootAt);
							
							makeDirectBullet = estimatedConnection.isDirect();
						}
						
						// bullet trajectory
						if (makeDirectBullet) {
							bullet.setX((int) shootAt.x).setY((int) shootAt.y);
							graphicEntityModule.commitEntityState(t, bullet);
						}
						else {
							Route bulletTrajectory = new Route(shootFrom, estimatedConnection.getIntersection1(), estimatedConnection.getIntersection2(), shootAt);
							
							List<Pair<Double, Vector2D>> bulletEvents = bulletTrajectory.getTravelWithRelativeTimestamps(0, 1);
							
							for (Pair<Double, Vector2D> timePos : bulletEvents) {
								Vector2D pos = timePos.getValue();
								double tnow = timePos.getKey() * (t - bulletStartTime) + bulletStartTime;
								
								bullet.setX((int) pos.x).setY((int) pos.y);
								graphicEntityModule.commitEntityState(tnow, bullet);
							}
						}
						
						// let bullet disappear
						bullet.setAlpha(0, Curve.IMMEDIATE);
						graphicEntityModule.commitEntityState(1, bullet);
						break;
					case FORWARD:
						if (lastStep) { // move to the end position (from starting or fighting position)
							timedPositioning = route.getTravelWithRelativeTimestamps(steps.size() <= 1 ? 0 : relativeFightPosition, 1);
						}
						else { // move to fighting position
							timedPositioning = route.getTravelWithRelativeTimestamps(0, relativeFightPosition);
						}
						break;
					case RETREAT:
						// move from fight to starting position
						timedPositioning = route.getTravelWithRelativeTimestamps(relativeFightPosition, 0);
						break;
				}
				
				// update number of troops
				troopText.setText(step.troops + "");
				
				if (timedPositioning == null) { // no movement, commit "standing still"
					troopText.setX(troops.getX()).setY(troops.getY() + TEXT_TROOPS_Y_OFFSET);
					graphicEntityModule.commitEntityState(t, troops, troopText);
				}
				else { // movement
					for (Pair<Double, Vector2D> timePos : timedPositioning) {
						Vector2D pos = timePos.getValue();
						double tnow = timePos.getKey() * (t - tStart) + tStart;
						
						troops.setX((int) pos.x).setY((int) pos.y);
						troopText.setX(troops.getX()).setY(troops.getY() + TEXT_TROOPS_Y_OFFSET);
						graphicEntityModule.commitEntityState(tnow, troops, troopText);
					}
				}
			}
		}
	}
	
	public void resetAnimations(TurnType turnType) {
		// Remove deployment text again
		if (turnType != TurnType.DEPLOY_TROOPS) {
			for (Field field : fieldText.keySet()) {
				Text deployAt = deployText.get(field);
				
				deployAt.setText("").setAlpha(0, Curve.EASE_OUT);
			}
			graphicEntityModule.commitEntityState(0.35, fieldText.values().toArray(new Entity[] {}));
		}
		
		// Move units and their text back to the original field and make them invisible
		if (turnType != TurnType.MOVE_TROOPS && cachedFieldPositions != null) {
			for (Pair<Field, Field> key : moveAnimations.keySet()) {
				Pair<SpriteAnimation, SpriteAnimation> spritesRedBlue = moveAnimations.get(key);
				Vector2D startPosition = cachedFieldPositions.get(key.getKey());
				
				for (SpriteAnimation troop : new SpriteAnimation[] {spritesRedBlue.getKey(), spritesRedBlue.getValue()}) {
					troop.setAlpha(0, Curve.NONE).setX((int) startPosition.x, Curve.NONE).setY((int) startPosition.y, Curve.NONE);
				}
				
				Text troopText = moveText.get(key);
				troopText.setX((int) startPosition.x, Curve.NONE).setY((int) startPosition.y + TEXT_TROOPS_Y_OFFSET, Curve.NONE).setAlpha(1, Curve.NONE).setText("");
			}
		}
		
		// Remove the PICK hands, once this phase is over
		for (Sprite hand : pickGraphics.values()) {
			hand.setAlpha(0, Curve.NONE);
			graphicEntityModule.commitEntityState(0.2, hand);
		}
	}
	
	// **********************************************************************
	// *** get/estimate resources
	// **********************************************************************
	
	private Rectangle2D getBounds(String txt, int fontSize) {
		Font font = new Font(txt, Font.PLAIN, fontSize);
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
				cachedRegionColors.put(region, (1 + region.getId().hashCode() * 1337) % 0xFFFFF);
			}
		}
		
		return cachedRegionColors;
	}
	
	private Map<Region, Polygon> getRegionPolynoms(Set<Region> regions) {
		Set<Field> fields = regions.stream().flatMap(r -> r.fields.stream()).collect(Collectors.toSet());
		Map<Field, Vector2D> fieldPositions = getPositions(fields);
		
		RegionGrid grid = new RegionGrid(regions, fieldPositions);
		grid.setWidth(GAME_FIELD_WIDTH);
		grid.setHeight(GAME_FIELD_HEIGHT);
		grid.setX(GAME_FIELD_X);
		grid.setY(GAME_FIELD_Y);
		grid.setResolution(2);
		grid.setDistNorm(1);
		
		return grid.createRegionPolygons(graphicEntityModule);
	}
	
	private Map<Field, Vector2D> estimateFieldPositions(Set<Field> fields) {
		Map<Field, Vector2D> positions = new HashMap<Field, Vector2D>();
		
		// right now, we use hard coded values
		if (fields.size() == 8) { // map_one_region
			double xDist = 0.25 * GAME_FIELD_WIDTH;
			double yDist = 0.4 * GAME_FIELD_HEIGHT;
			
			for (Field field : fields) {
				int id = field.id;
				boolean mirrorX = id >= fields.size() / 2;
				boolean mirrorY = false;
				id = id % (fields.size() / 2);
				
				double xDiff = xDist / 2;
				double yDiff = yDist;
				
				if (id == 1) {
					xDiff += xDist;
				}
				
				while (id >= 1) {
					yDiff -= yDist;
					id -= 2;
				}
				
				if (mirrorX)
					xDiff *= -1;
				
				if (mirrorY)
					yDiff *= -1;
				
				double x = GAME_FIELD_WIDTH / 2 - xDiff;
				double y = GAME_FIELD_HEIGHT / 2 - yDiff;
				
				positions.put(field, new Vector2D(GAME_FIELD_X + x, GAME_FIELD_Y + y));
			}
		}
		else if (fields.size() == 18) { // map_two_regions
			double xDist = 0.15 * GAME_FIELD_WIDTH;
			double yDist = 0.3 * GAME_FIELD_HEIGHT;
			
			for (Field field : fields) {
				int id = field.id;
				boolean mirrorX = id >= fields.size() / 2;
				boolean mirrorY = false;
				id = id % (fields.size() / 2);
				
				double xDiff = 0;
				double yDiff = 0;
				
				if (id == 2) {
					xDiff = 3 * xDist;
					yDiff = 0;
				}
				else if (id < 2 || id > 6) {
					int idX = id < 2 ? id : id - 7;
					xDiff = (2 - idX) * xDist;
					yDiff = 1.5 * yDist;
					
					mirrorY = id > 6;
				}
				else { // 3, 4, 5 6
					int idX = id < 5 ? id : id - 2;
					xDiff = idX == 3 ? 2.25 * xDist : .5 * xDist;
					yDiff = 0.5 * yDist;
					
					mirrorY = id > 4;
				}
				
				if (mirrorX)
					xDiff *= -1;
				
				if (mirrorY)
					yDiff *= -1;
				
				double x = GAME_FIELD_WIDTH / 2 - xDiff;
				double y = GAME_FIELD_HEIGHT / 2 - yDiff;
				
				positions.put(field, new Vector2D(GAME_FIELD_X + x, GAME_FIELD_Y + y));
			}
		}
		else if (fields.size() == 26) { // map_five_regions
			double xDist = 0.1 * GAME_FIELD_WIDTH;
			double yDist = 0.2 * GAME_FIELD_HEIGHT;
			double regionMoveX = 0.85;
			
			for (Field field : fields) {
				int id = field.id;
				boolean mirrorX = id >= fields.size() / 2;
				boolean mirrorY = false;
				id = id % (fields.size() / 2);
				
				double xDiff = 0;
				double yDiff = 0;
				
				if (id >= 10 && id <= 12) { // region C
					xDiff = xDist;
					yDiff = -(id - 11) * yDist;
				}
				else if (id >= 0 && id <= 5) {// region A
					int row = 0;
					
					if (id < 3) {
						xDiff = regionMoveX * (5 - id) * xDist;
						row = 0;
					}
					else if (id < 5) {
						xDiff = regionMoveX * (7.5 - id) * xDist;
						row = 1;
					}
					else {
						xDiff = regionMoveX * (5 - 1) * xDist;
						row = 2;
					}
					
					yDiff = 0.75 * (3 - row) * yDist;
				}
				else { // region B
					int row = 0;
					
					if (id == 6) {
						xDiff = regionMoveX * (4) * xDist;
						row = 0;
					}
					else if (id == 9) {
						xDiff = regionMoveX * (4) * xDist;
						row = 2;
					}
					else {
						xDiff = regionMoveX * (4) * xDist + 1.5 * (7.5 - id) * xDist;
						row = 1;
					}
					
					yDiff = -0.75 * (row + 1) * yDist;
				}
				
				if (mirrorX)
					xDiff *= -1;
				
				if (mirrorY)
					yDiff *= -1;
				
				double x = GAME_FIELD_WIDTH / 2 - xDiff;
				double y = GAME_FIELD_HEIGHT / 2 - yDiff;
				
				positions.put(field, new Vector2D(GAME_FIELD_X + x, GAME_FIELD_Y + y));
			}
		}
		else {
			// do random placement if map not found (improvement would be to mirror half...)
			for (Field field : fields) {
				double x = RandomUtil.getInstance().nextFloat() * GAME_FIELD_WIDTH;
				double y = RandomUtil.getInstance().nextFloat() * GAME_FIELD_HEIGHT;
				positions.put(field, new Vector2D(GAME_FIELD_X + x, GAME_FIELD_Y + y));
			}
		}
		
		return positions;
	}
}
