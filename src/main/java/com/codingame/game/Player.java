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
	
	public List<Action> getMoves() throws TimeoutException, NumberFormatException, InvalidAction {
		List<Action> actions = new ArrayList<Action>();
		String output = getOutputs().get(0);
		
		// Multiple moves are separated by a semicolon ";"
		for (String actionStr : output.split(";")) {
			actions.add(parseMove(actionStr.trim()));
		}
		
		return actions;
	}
	
	private Action parseMove(String output) throws NumberFormatException, InvalidAction {
		Action action = null;
		String[] parts = output.split(" ");
		
		if (parts.length < 1) {
			throw new InvalidAction("No action is given");
		} 
		
		String type = parts[0];
		
		if (Action.Type.RANDOM.toString().equals(type)) {
			return new Action(Action.Type.RANDOM);
		} else if (parts.length < 2) {
			throw new InvalidAction("Missing field id in output: "+output);
		}
		
		if (Action.Type.CHOOSE_STARTING_POSITION.toString().equals(type)) {
			action = new Action(Action.Type.CHOOSE_STARTING_POSITION, Integer.parseInt(parts[1]));
		} else if (Action.Type.DEPLOY_TROOPS.toString().equals(type)) {
			if (parts.length < 3) {
				throw new InvalidAction("Missing number of troops for action: "+type);
			}
			
			action = new Action(Action.Type.DEPLOY_TROOPS, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
		} else if (Action.Type.MOVEMENT.toString().equals(type)) {
			if (parts.length < 3) {
				throw new InvalidAction("Missing source id for action: "+type);
			}
			
			if (parts.length < 4) {
				throw new InvalidAction("Missing number of troops for action: "+type);
			}
			
			action = new Action(Action.Type.MOVEMENT, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]),  Integer.parseInt(parts[3]));
		} else {
			throw new InvalidAction("Invalid action type: "+type);
		}
		
		
		return action;
	}
}
