package com.codingame.game.view;

import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.Player;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
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
}
