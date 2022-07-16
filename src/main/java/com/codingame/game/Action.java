package com.codingame.game;

import com.codingame.game.core.Movement;

/**
 * An action that was parsed from the player's output.
 */
public class Action {
	
	public static final int NO_SELECTION = -1;
	
	public static enum Type {
		CHOOSE_STARTING_POSITION, //
		DEPLOY_TROOPS, //
		MOVEMENT; //
		
		public String toString() {
			switch(this) {
			case CHOOSE_STARTING_POSITION: return "PICK";
			case DEPLOY_TROOPS: return "DEPLOY";
			case MOVEMENT: return "MOVE";
			default:
				throw new IllegalStateException("Unknown action type");
			}
		}
	}
	
	private Type type;
	private int targetId; // the id of the target field (used in all move types)
	private int sourceId; // the id of the source field (only for MOVEMENT)
	private int numTroops; // the number of troops that are deployed / moved (must be greater than 0)
	
	public Action(Type type, int targetId) {
		this(type, targetId, NO_SELECTION, 0);
	}
	
	public Action(Type type, int targetId, int numTroops) {
		this(type, targetId, NO_SELECTION, numTroops);
	}
	
	public Action(Type type, int targetId, int sourceId, int numTroops) {
		this.type = type;
		this.targetId = targetId;
		this.sourceId = sourceId;
		this.numTroops = numTroops;
	}
	
	public Type getType() {
		return type;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public int getSourceId() {
		return sourceId;
	}
	
	public int getNumTroops() {
		return numTroops;
	}
	
	public String toString() {
		switch(type) {
		case CHOOSE_STARTING_POSITION: return String.format("%s %d", type, targetId);
		case DEPLOY_TROOPS: return String.format("%s %d %d", type, targetId, numTroops);
		case MOVEMENT: return String.format("%s %d %d %d", type, targetId, sourceId, numTroops);
		default:
			throw new IllegalStateException("Invalid action state");
		}
	}
}
