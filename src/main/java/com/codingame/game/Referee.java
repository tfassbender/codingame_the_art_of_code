package com.codingame.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.codingame.game.Action.Type;
import com.codingame.game.build.RandomUtil;
import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
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
	
	@Override
	public void init() {
		RandomUtil.init(gameManager.getSeed());
		league = League.getByLevel(gameManager.getLeagueLevel());
		gameManager.setFrameDuration(FRAME_DURATION);
		gameManager.setMaxTurns(MAX_TURNS);
		gameManager.setFirstTurnMaxTime(1000);
		gameManager.setTurnMaxTime(50); // TODO are 50ms enough?
		
		map = new StaticMapGenerator().createMapFiveRegions();
		turnType = TurnType.CHOOSE_STARTING_FIELDS;
		
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
		
		List<Action> actions1 = getActions(player1, Owner.PLAYER_1);
		List<Action> actions2 = getActions(player2, Owner.PLAYER_2);
		
		// add the owner of the action to the action object
		actions1.forEach(action -> action.setOwner(Owner.PLAYER_1));
		actions2.forEach(action -> action.setOwner(Owner.PLAYER_2));
		
		if (actions1 != null && actions2 != null) {
			executeActions(actions1, actions2);
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
		
		if (league.pickCommandEnabled) {
			// next line: two integers - the number of fields for each player to choose (your input is always first; 0 in all turn types but CHOOSE_STARTING_FIELDS)
			player.sendInputLine(map.getStartingFieldChoice().getStartingFieldsLeft(playerId) + " " + map.getStartingFieldChoice().getStartingFieldsLeft(playerId.getOpponent()));
		}
		else {
			// next line: two integers - ignore in this league
			player.sendInputLine("0 0");
		}
		
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
	
	private List<Action> getActions(Player player, Owner owner) {
		try {
			List<Action> actions = player.getMoves();
			validateActions(actions, owner);
			return actions;
		}
		catch (TimeoutException e) {
			player.deactivate(String.format("$%d timeout!", player.getIndex()));
		}
		catch (NumberFormatException e) {
			player.deactivate("Wrong output!");
			player.setScore(-1);
			endGame();
		}
		catch (InvalidActionException e) {
			String deactivateMessage = e.getMessage();
			player.deactivate(deactivateMessage);
			player.setScore(-1);
			endGame();
		}
		
		return null;
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
					if (turnType == TurnType.CHOOSE_STARTING_FIELDS) {
						throw new InvalidActionException("The action " + Type.WAIT + " cannot be used in '" + TurnType.CHOOSE_STARTING_FIELDS + //
								"' turns. Use " + Type.RANDOM + " if you don't want to choose the fields yourself.");
					}
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
				int allowedDeployments = map.calculateDeployableTroops(player);
				if (totalDeployedTroops > allowedDeployments) {
					throw new InvalidActionException("Cannot " + Type.DEPLOY + " " + totalDeployedTroops + //
							" troops in total. You can only deploy " + allowedDeployments + " troops in this turn.");
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
		// TODO insert end game winner checks here
		
		gameManager.endGame();
	}
	
	private void executeActions(List<Action> actions1, List<Action> actions2) {
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
