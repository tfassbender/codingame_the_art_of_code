package com.codingame.game;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.codingame.game.Action.Type;
import com.codingame.game.build.MapGenerator;
import com.codingame.game.build.RandomUtil;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.core.TurnType;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.View;
import com.codingame.gameengine.core.AbstractPlayer.TimeoutException;
import com.codingame.gameengine.core.AbstractReferee;
import com.codingame.gameengine.core.GameManager;
import com.codingame.gameengine.core.MultiplayerGameManager;
import com.codingame.gameengine.module.endscreen.EndScreenModule;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.google.inject.Inject;

public class Referee extends AbstractReferee {
	
	public static final int FRAME_DURATION = 2000;
	public static final int MAX_TURNS = 200;
	public static final int NUM_PLAYERS = 2;
	
	@Inject
	private MultiplayerGameManager<Player> gameManager;
	@Inject
	private GraphicEntityModule graphicEntityModule;
	@Inject
	private EndScreenModule endScreenModule;
	
	private View view;
	
	private League league;
	private GameMap map;
	
	private TurnType turnType;
	
	private boolean firstDeployment = true;
	
	@Override
	public void init() {
		RandomUtil.init(gameManager.getRandom());
		
		league = League.getByLevel(gameManager.getLeagueLevel());
		gameManager.setFrameDuration(FRAME_DURATION);
		gameManager.setMaxTurns(MAX_TURNS);
		gameManager.setFirstTurnMaxTime(1000);
		gameManager.setTurnMaxTime(50);
		
		turnType = TurnType.CHOOSE_STARTING_FIELDS;
		
		Pair<GameMap, Map<Field, Vector2D>> generatedMap = MapGenerator.generateMap();
		map = generatedMap.getKey();
		Map<Field, Vector2D> initialPositions = generatedMap.getValue();
		
		view = new View(graphicEntityModule, map, initialPositions);
		view.drawBackground();
		view.drawPlayerInfos(gameManager.getPlayer(0), gameManager.getPlayer(1));
		view.drawLegend(map.regions);
		view.drawRegions(map.regions);
		view.drawConnections(map.connections, map.regions);
		view.drawFields(map.fields);
		
		sendInitialInput();
	}
	
	private void sendInitialInput() {
		// send a description of the map to both players
		for (Player player : gameManager.getPlayers()) {
			// next line: one integer - the NUMBER_OF_REGIONS
			player.sendInputLine(Integer.toString(map.regions.size()));
			// next NUMBER_OF_REGIONS inputs:
			// - one line: two integers - region id and the number of bonus troops for this region
			for (Region region : map.regions) {
				player.sendInputLine(Integer.toString(region.id) + " " + Integer.toString(region.bonusTroops));
			}
			
			// first line: one integer - the NUMBER_OF_FIELDS in the map
			player.sendInputLine(Integer.toString(map.fields.size()));
			for (Field field : map.fields) {
				// next NUMBER_OF_FIELDS lines: two integers - the field id and the region id, the field belongs to
				player.sendInputLine(Integer.toString(field.id) + " " + Integer.toString(map.getRegionForFieldById(field.id).get().id));
			}
			
			// next line: one integer - the NUMBER_OF_CONNECTIONS between fields
			player.sendInputLine(Integer.toString(map.connections.size()));
			for (Pair<Field, Field> connection : map.connections) {
				// next NUMBER_OF_CONNECTIONS lines: two integers - the SOURCE_ID and the TARGET_ID of the fields that are connected (bidirectional)
				player.sendInputLine(connection.getKey().id + " " + connection.getValue().id);
			}
			
			// next line: one string - either UPPER or LOWER - the part of the field (identified by id) in which you have the higher priority to choose a starting field
			if (gameManager.getPlayers().indexOf(player) == 0) {
				player.sendInputLine("LOWER");
			}
			else {
				player.sendInputLine("UPPER");
			}
		}
	}
	
	@Override
	public void gameTurn(int turn) {
		view.resetAnimations(turnType);
		map.resetEvents();
		
		Player player1 = gameManager.getPlayer(0);
		Player player2 = gameManager.getPlayer(1);
		
		List<Action> actions1 = Collections.emptyList();
		List<Action> actions2 = Collections.emptyList();
		
		if (turnType == TurnType.CHOOSE_STARTING_FIELDS) {
			// the player only take a turn if there are starting fields left to choose
			boolean player1Active = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1) > 0;
			boolean player2Active = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2) > 0;
			
			if (player1Active)
				sendTurnInput(player1, Owner.PLAYER_1);
			
			if (player2Active)
				sendTurnInput(player2, Owner.PLAYER_2);
			
			if (player1Active) {
				player1.execute();
				actions1 = getActions(player1, Owner.PLAYER_1);
			}
			
			if (player2Active) {
				player2.execute();
				actions2 = getActions(player2, Owner.PLAYER_2);
			}
		}
		else {
			// the turn type is DEPLOY_TROOPS or MOVE_TROOPS, so both players take a turn
			sendTurnInput(player1, Owner.PLAYER_1);
			sendTurnInput(player2, Owner.PLAYER_2);
			
			player1.execute();
			player2.execute();
			
			actions1 = getActions(player1, Owner.PLAYER_1);
			actions2 = getActions(player2, Owner.PLAYER_2);
		}
		
		// the game might end here, because of wrong outputs
		// in this case, we can not do anything more in this game turn
		if (gameManager.isGameEnd()) {
			return;
		}
		
		// replace WAIT during CHOOSE_STARTING_FIELDS with RANDOM
		if (turnType == TurnType.CHOOSE_STARTING_FIELDS) {
			actions1.replaceAll(action -> action.getType() == Type.WAIT ? new Action(Type.RANDOM) : action);
			actions2.replaceAll(action -> action.getType() == Type.WAIT ? new Action(Type.RANDOM) : action);
		}
		
		// add the owner of the action to the action object
		actions1.forEach(action -> action.setOwner(Owner.PLAYER_1));
		actions2.forEach(action -> action.setOwner(Owner.PLAYER_2));
		
		executeActions(actions1, actions2);
		
		// Animate the actions
		
		switch (turnType) {
			case CHOOSE_STARTING_FIELDS:
				view.animatePicks(map.fields, map.getPicksPerformed());
				break;
			case DEPLOY_TROOPS:
				view.animateDeployments(actions1, actions2);
				break;
			case MOVE_TROOPS:
				//view.animateMovements(map.fields, actions1, actions2);
				view.animateMovements(map.getEvents(), map.fields);
				break;
		}
		
		// update the turn type and other turn values
		
		switch (turnType) {
			case CHOOSE_STARTING_FIELDS:
				if (map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1) > 0 || //
						map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2) > 0) {
					// not all starting fields are chosen yet
					turnType = TurnType.CHOOSE_STARTING_FIELDS;
				}
				else {
					// all starting fields are chosen 
					
					// all other fields are occupied by 2 neutral troops per field
					map.deployNeutralTroops();
					
					// deploy troops in the next turn
					turnType = TurnType.DEPLOY_TROOPS;
				}
				break;
			case DEPLOY_TROOPS:
				firstDeployment = false;
				turnType = TurnType.MOVE_TROOPS; // switch between deploying and moving troops
				break;
			case MOVE_TROOPS:
				map.resetRoundingLosses();
				turnType = TurnType.DEPLOY_TROOPS; // switch between deploying and moving troops
				break;
			default:
				throw new IllegalStateException("Unknown turn type: " + turnType);
		}
		
		// update the scores and statistics of each player
		int fieldsPlayer1 = map.getNumFieldsControlledByPlayer(Owner.PLAYER_1);
		int fieldsPlayer2 = map.getNumFieldsControlledByPlayer(Owner.PLAYER_2);
		int troopsPlayer1 = map.getNumTroopsControlledByPlayer(Owner.PLAYER_1);
		int troopsPlayer2 = map.getNumTroopsControlledByPlayer(Owner.PLAYER_2);
		int deployableTroopsPlayer1 = map.calculateDeployableTroops(Owner.PLAYER_1, firstDeployment);
		int deployableTroopsPlayer2 = map.calculateDeployableTroops(Owner.PLAYER_2, firstDeployment);
		
		view.updatePlayerStats(Owner.PLAYER_1, fieldsPlayer1, troopsPlayer1, deployableTroopsPlayer1);
		view.updatePlayerStats(Owner.PLAYER_2, fieldsPlayer2, troopsPlayer2, deployableTroopsPlayer2);
		view.updateFields(map.fields);
		view.updateRegionLegend(map.regions);
		
		player1.setScore(fieldsPlayer1);
		player2.setScore(fieldsPlayer2);
		
		// check whether the game has ended
		if (turnType != TurnType.CHOOSE_STARTING_FIELDS && (fieldsPlayer1 == 0 || fieldsPlayer2 == 0)) {
			gameManager.endGame();
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
		player.sendInputLine(map.calculateDeployableTroops(playerId, firstDeployment) + " " + //
				map.calculateDeployableTroops(playerId.getOpponent(), firstDeployment));
		
		// next line: two integers - the number of fields for each player to choose (your input is always first; 0 in all turn types but CHOOSE_STARTING_FIELDS)
		player.sendInputLine(map.getStartingFieldChoice().getStartingFieldsLeft(playerId) + " " + //
				map.getStartingFieldChoice().getStartingFieldsLeft(playerId.getOpponent()));
		
		// next NUMBER_OF_FIELDS lines: three integers - the FIELD_ID, the NUMBER_OF_TROOPS in this field, the OWNER of this field 
		//                              (1 if the field is controlled by you; 2 if it's controlled by the opponent player; 0 if it's neutral)
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
	
	private List<Action> getActions(Player player, Owner owner) {
		try {
			List<Action> actions = player.getMoves();
			validateActions(actions, owner);
			return actions;
		}
		catch (TimeoutException e) {
			player.deactivate(String.format("$%d timeout!", player.getIndex()));
			player.setScore(-1);
			endGame();
		}
		catch (NumberFormatException e) {
			player.deactivate("Wrong output! " + e.getMessage());
			player.setScore(-1);
			endGame();
		}
		catch (InvalidActionException e) {
			player.deactivate(e.getMessage());
			player.setScore(-1);
			endGame();
		}
		
		return Collections.emptyList();
	}
	
	private void validateActions(List<Action> actions, Owner player) throws InvalidActionException {
		Optional<Field> pickedField;
		Map<Integer, List<Integer>> movementTargets = new HashMap<>();
		int[] sumOfTroopsMovedOutOfField = new int[map.fields.size()];
		
		// check all actions by themselves
		
		for (Action action : actions) {
			switch (action.getType()) {
				case PICK:
					if (turnType != TurnType.CHOOSE_STARTING_FIELDS) {
						throw createInvalidActionTypeException(Type.PICK, TurnType.CHOOSE_STARTING_FIELDS);
					}
					if (!league.pickCommandEnabled) {
						throw new InvalidActionException("The command " + Type.PICK + " is not enabled in this league. Please use " + //
								Type.RANDOM + " instead.");
					}
					pickedField = map.getFieldById(action.getTargetId());
					if (!pickedField.isPresent()) {
						throw new InvalidActionException("A field with the id " + action.getTargetId() + " does not exist.");
					}
					else if (pickedField.get().getOwner() != Owner.NEUTRAL) {
						throw new InvalidActionException("The field with the id " + action.getTargetId() + " was already picked.");
					}
					
					if (actions.size() > 1) {
						throw new InvalidActionException("You can only " + Type.PICK + " one field per turn.");
					}
					break;
				case DEPLOY:
					if (turnType != TurnType.DEPLOY_TROOPS) {
						throw createInvalidActionTypeException(Type.DEPLOY, TurnType.DEPLOY_TROOPS);
					}
					pickedField = map.getFieldById(action.getTargetId());
					if (!pickedField.isPresent()) {
						throw new InvalidActionException("A field with the id " + action.getTargetId() + " does not exist.");
					}
					else if (pickedField.get().getOwner() != player) {
						throw new InvalidActionException("Cannot " + Type.DEPLOY + " to field " + action.getTargetId() + //
								". You don't controll this field.");
					}
					else if (action.getNumTroops() <= 0) {
						throw new InvalidActionException("Cannot " + Type.DEPLOY + " " + action.getNumTroops() + " troops. You have to " + //
								Type.DEPLOY + " at least 1 troop.");
					}
					break;
				case MOVE:
					if (turnType != TurnType.MOVE_TROOPS) {
						throw createInvalidActionTypeException(Type.MOVE, TurnType.MOVE_TROOPS);
					}
					Optional<Field> sourceField = pickedField = map.getFieldById(action.getSourceId());
					Optional<Field> targetField = pickedField = map.getFieldById(action.getTargetId());
					if (!sourceField.isPresent()) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " from field " + action.getSourceId() + //
								". A field with the id " + action.getSourceId() + " does not exist.");
					}
					if (!targetField.isPresent()) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " to field " + action.getTargetId() + //
								". A field with the id " + action.getTargetId() + " does not exist.");
					}
					if (sourceField.get().getOwner() != player) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " from field " + action.getSourceId() + //
								". You don't controll this field.");
					}
					if (action.getSourceId() == action.getTargetId()) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " from field " + action.getSourceId() + //
								" to field " + action.getTargetId() + ". A move must be to a different field.");
					}
					if (!map.isFieldsConnected(action.getSourceId(), action.getTargetId())) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " from field " + action.getSourceId() + //
								" to field " + action.getTargetId() + ". The fields are not connected.");
					}
					if (action.getNumTroops() <= 0) {
						throw new InvalidActionException("Cannot execute a " + Type.MOVE + " command with no troops. " + //
								"You must move at least 1 troop.");
					}
					
					sumOfTroopsMovedOutOfField[action.getSourceId()] += action.getNumTroops();
					if (sumOfTroopsMovedOutOfField[action.getSourceId()] > sourceField.get().getTroops()) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " a total number of " + //
								sumOfTroopsMovedOutOfField[action.getSourceId()] + " (or more) troops from field " + action.getSourceId() + //
								". The field only contains " + sourceField.get().getTroops() + " troops.");
					}
					
					List<Integer> targetsFromSourceField = movementTargets.computeIfAbsent(action.getSourceId(), i -> new ArrayList<>());
					if (targetsFromSourceField.contains(action.getTargetId())) {
						throw new InvalidActionException("Cannot " + Type.MOVE + " from field " + action.getSourceId() + //
								" to field " + action.getTargetId() + " with multiple commands. " + //
								"Only one move with the same source and target is allowed.");
					}
					targetsFromSourceField.add(action.getTargetId());
					break;
				case RANDOM:
					if (turnType != TurnType.CHOOSE_STARTING_FIELDS) {
						throw createInvalidActionTypeException(Action.Type.RANDOM, TurnType.CHOOSE_STARTING_FIELDS);
					}
					break;
				case WAIT:
					// always valid
					break;
				default:
					throw new IllegalStateException("Unknown action type: " + action.getType());
			}
		}
		
		// all actions are valid by themselves - check the combination
		
		switch (turnType) {
			case CHOOSE_STARTING_FIELDS:
				// do nothing here
				break;
			case DEPLOY_TROOPS:
				int totalDeployedTroops = actions.stream().filter(action -> action.getType() == Type.DEPLOY).mapToInt(Action::getNumTroops).sum();
				int allowedDeployments = map.calculateDeployableTroops(player, firstDeployment);
				if (totalDeployedTroops > allowedDeployments) {
					throw new InvalidActionException("Cannot " + Type.DEPLOY + " " + totalDeployedTroops + //
							" troops in total. You can only " + Type.DEPLOY + " " + allowedDeployments + " troops in this turn.");
				}
				break;
			case MOVE_TROOPS:
				break;
			default:
				throw new IllegalStateException("Unknown turn type: " + turnType);
		}
		
		if (actions.stream().anyMatch(action -> action.getType() == Type.WAIT) && actions.size() > 1) {
			throw new InvalidActionException(Type.WAIT + " commands cannot be mixed with other commands.");
		}
	}
	
	private InvalidActionException createInvalidActionTypeException(Action.Type actionType, TurnType expectedTurn) {
		return new InvalidActionException("The action " + actionType + " cannot be used in this turn, but only in turns of type '" + expectedTurn + "'");
	}
	
	private void endGame() {
		Player winner = null;
		
		Player player1 = gameManager.getPlayers().get(0);
		Player player2 = gameManager.getPlayers().get(1);
		
		if (player1.getScore() > player2.getScore()) {
			winner = player1;
		}
		else if (player1.getScore() < player2.getScore()) {
			winner = player2;
		}
		else {
			gameManager.addToGameSummary(GameManager.formatSuccessMessage("Draw! Both players conquered " + player1.getScore() + " fields."));
		}
		
		if (winner != null) {
			gameManager.addToGameSummary(GameManager.formatSuccessMessage(winner.getNicknameToken() + " won!"));
		}
		
		gameManager.endGame();
	}
	
	@Override
	public void onEnd() {
		Player p0 = gameManager.getPlayers().get(0);
		Player p1 = gameManager.getPlayers().get(1);
		int[] scores = new int[] {p0.getScore(), p1.getScore()};
		String[] texts = new String[] {p0.getScore() + " fields conquered", p1.getScore() + " fields conquered"};
		endScreenModule.setScores(scores, texts);
		endScreenModule.setTitleRankingsSprite("logo.png");
	}
	
	private void executeActions(List<Action> actions1, List<Action> actions2) {
		if (turnType == TurnType.DEPLOY_TROOPS) {
			// calculate how many troops are not deployed in this turn, so they can be deployed in the next turn
			
			int deployedTroops1 = actions1.stream().filter(action -> action.getType() == Type.DEPLOY).mapToInt(Action::getNumTroops).sum();
			int deployedTroops2 = actions2.stream().filter(action -> action.getType() == Type.DEPLOY).mapToInt(Action::getNumTroops).sum();
			
			int sparedTroops1 = map.calculateDeployableTroops(Owner.PLAYER_1, firstDeployment) - deployedTroops1;
			int sparedTroops2 = map.calculateDeployableTroops(Owner.PLAYER_2, firstDeployment) - deployedTroops2;
			
			map.setSparedDeployingTroops(sparedTroops1, Owner.PLAYER_1);
			map.setSparedDeployingTroops(sparedTroops2, Owner.PLAYER_2);
		}
		
		int minMoves = Math.min(actions1.size(), actions2.size());
		
		// every two moves of the players are executed simultaneously (in the order of the list) 
		for (int i = 0; i < minMoves; i++) {
			map.executeSimultaneously(actions1.get(i), actions2.get(i));
		}
		
		// if one player has committed more moves than the other, there is no need for a simultaneous execution
		for (int i = minMoves; i < actions1.size(); i++) {
			map.executeIndependent(actions1.get(i));
		}
		for (int i = minMoves; i < actions2.size(); i++) {
			map.executeIndependent(actions2.get(i));
		}
	}
}
