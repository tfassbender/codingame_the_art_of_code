package com.codingame.game;

import java.util.List;

import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.core.StartingFieldChoice;
import com.codingame.game.core.TurnType;
import com.codingame.game.util.Pair;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
	
	public static final int FRAME_DURATION = 800;
	public static final int MAX_TURNS = 200;
	public static final int NUM_PLAYERS = 2;
	
	@Inject
	private MultiplayerGameManager<Player> gameManager;
	
	private League league;
	private GameMap map;
	
	private TurnType turnType; //TODO this needs to be updated
	private StartingFieldChoice startingFieldChoice;
	
	@Override
	public void init() {
		league = League.getByLevel(gameManager.getLeagueLevel());
		gameManager.setFrameDuration(FRAME_DURATION);
		gameManager.setMaxTurns(MAX_TURNS);
		
		gameManager.setFirstTurnMaxTime(1000);
		gameManager.setTurnMaxTime(50); // TODO are 50ms enough?
		
		map = new StaticMapGenerator().createMapFiveRegions();
		turnType = TurnType.CHOOSE_STARTING_FIELDS;
		startingFieldChoice = new StartingFieldChoice(map.fields.size());
		
		sendInitialInput();
	}
	
	private void sendInitialInput() {
		// send a description of the map to both players
		for (Player player : gameManager.getPlayers()) {
			// first line: one integer - the NUMBER_OF_FIELDS in the map
			player.sendInputLine(Integer.toString(map.fields.size()));
			for (Field field : map.fields) {
				// next NUMBER_OF_FIELDS lines: two integers - the ID of the field, the number of troops on the field
				player.sendInputLine(field.id + " " + field.getTroops());
			}
			
			// next line: one integer - the NUMBER_OF_CONNECTIONS between fields
			player.sendInputLine(Integer.toString(map.connections.size()));
			for (Pair<Field, Field> connection : map.connections) {
				// next NUMBER_OF_CONNECTIONS lines: two integers - the SOURCE_ID and the TARGET_ID of the fields that are connected (bidirectional)
				player.sendInputLine(connection.getKey().id + " " + connection.getValue().id);
			}
			
			// next line: one integer - the NUMBER_OF_REGIONS
			player.sendInputLine(Integer.toString(map.regions.size()));
			// next NUMBER_OF_REGIONS inputs:
			// - one line: one integer - the number of bonus troops for this region
			// - one line: one integer - the NUMBER_OF_FIELDS_PER_REGION
			// - next NUBER_OF_FIELDS_PER_REGION lines: one integer - the id of the field that belongs to the region
			for (Region region : map.regions) {
				player.sendInputLine(Integer.toString(region.bonusTroops));
				player.sendInputLine(Integer.toString(region.fields.size()));
				for (Field field : region.fields) {
					player.sendInputLine(Integer.toString(field.id));
				}
			}
		}
	}
	
	@Override
	public void gameTurn(int turn) {
		Player player1 = gameManager.getPlayer(0);
		Player player2 = gameManager.getPlayer(1);
		
		sendTurnInput(player1, Owner.PLAYER_1);
		sendTurnInput(player2, Owner.PLAYER_2);
		
		player1.execute();
		player2.execute();
		
		for (Player player : gameManager.getActivePlayers()) {
			try {
				List<String> outputs = player.getOutputs();
				// Check validity of the player output and compute the new game state
			}
			catch (TimeoutException e) {
				player.deactivate(String.format("$%d timeout!", player.getIndex()));
			}
		}
	}
	
	/**
	 * Send the input for one game turn to the player. 
	 * NOTE: The input for a turn has the same format for every turn type, so the player knows what to expect.
	 */
	private void sendTurnInput(Player player, Owner playerId) {
		// first line: one string - the name of the turn type (CHOOSE_STARTING_FIELDS, DEPLOY_TROOPS or MOVE_TROOPS)
		player.sendInputLine(turnType.name());
		
		long playersFields = map.fields.stream().filter(field -> field.getOwner() == playerId).count();
		long otherPlayersFields = map.fields.stream().filter(field -> field.getOwner() == playerId.getOpponent()).count();
		// next line: two integers - the number of the fields that are held by each player (your input is always first)
		player.sendInputLine(playersFields + " " + otherPlayersFields);
		
		// next line: two integers - the number of troops that each player can deploy (your input is always first; 0 in all turn types but DEPLOY_TROOPS)
		player.sendInputLine(map.calculateDeployableTroops(playerId) + " " + map.calculateDeployableTroops(playerId.getOpponent()));
		
		// TODO enable only in higher leagues
		// next line: two integers - the number of fields for each player to choose (your input is always first; 0 in all turn types but CHOOSE_STARTING_FIELDS)
		player.sendInputLine(startingFieldChoice.getStartingFieldsLeft(playerId) + " " + startingFieldChoice.getStartingFieldsLeft(playerId.getOpponent()));
		
		// next line: the NUMBER_OF_FIELDS on the map
		player.sendInputLine(Integer.toString(map.fields.size()));
		// next NUMBER_OF_FIELDS lines: three integers - the FIELD_ID, the NUMBER_OF_TROOPS in this field, the OWNER of this field (1 if the field is controlled by you; 2 if it's controlled by the opponent player; 0 if it's neutral)
		for (Field field : map.fields) {
			int owner = 0;
			if (field.getOwner() == playerId) {
				owner = 1;
			}
			else if (field.getOwner() == playerId.getOpponent()) {
				owner = 2;
			}
			
			player.sendInputLine(field.id + " " + field.getTroops() + " " + owner);
		}
	}
}
