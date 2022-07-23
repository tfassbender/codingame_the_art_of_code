package com.codingame.game;

import java.util.ArrayList;
import java.util.List;

import com.codingame.gameengine.core.AbstractMultiplayerPlayer;

// Uncomment the line below and comment the line under it to create a Solo Game
// public class Player extends AbstractSoloPlayer {
public class Player extends AbstractMultiplayerPlayer {
	
	@Override
	public int getExpectedOutputLines() {
		// Returns the number of expected lines of outputs for a player
		return 1;
	}
	
	public List<Action> getMoves() throws TimeoutException, NumberFormatException, InvalidActionException {
		List<Action> actions = new ArrayList<Action>();
		String output = getOutputs().get(0);
		
		// Multiple moves are separated by a semicolon ";"
		for (String actionStr : output.split(";")) {
			if (actionStr.trim().length() != 0)
				actions.add(parseMove(actionStr.trim()));
		}
		
		if (actions.size() == 0) {
			throw new InvalidActionException("No action was given");
		}
		
		return actions;
	}
	
	private Action parseMove(String output) throws NumberFormatException, InvalidActionException {
		Action action = null;
		String[] parts = output.split(" ");
		
		if (parts.length < 1) {
			throw new InvalidActionException("No action is given");
		}
		
		String type = parts[0];
		
		if (Action.Type.RANDOM.toString().equals(type)) {
			return new Action(Action.Type.RANDOM);
		}
		else if (Action.Type.WAIT.toString().equals(type)) {
			return new Action(Action.Type.WAIT);
		}
		else if (parts.length < 2) {
			throw new InvalidActionException("Missing field id in output: " + output);
		}
		
		if (Action.Type.PICK.toString().equals(type)) {
			action = new Action(Action.Type.PICK, Integer.parseInt(parts[1]));
		}
		else if (Action.Type.DEPLOY.toString().equals(type)) {
			if (parts.length < 3) {
				throw new InvalidActionException("Missing number of troops for action: " + type);
			}
			
			action = new Action(Action.Type.DEPLOY, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		}
		else if (Action.Type.MOVE.toString().equals(type)) {
			if (parts.length < 3) {
				throw new InvalidActionException("Missing target id for action: " + type);
			}
			
			if (parts.length < 4) {
				throw new InvalidActionException("Missing number of troops for action: " + type);
			}
			
			action = new Action(Action.Type.MOVE, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]), Integer.parseInt(parts[3]));
		}
		else {
			throw new InvalidActionException("Invalid action type: " + type);
		}
		
		return action;
	}
}
