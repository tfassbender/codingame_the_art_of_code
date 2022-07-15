package com.codingame.game;

/**
 * An action that was parsed from the player's output.
 */
public class Action {
	
	public static final int NO_SELECTION = -1;
	
	public static enum Type {
		CHOOSE_STARTING_POSITION, //
		DEPLOY_TROOPS, //
		MOVEMENT, //
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
}
