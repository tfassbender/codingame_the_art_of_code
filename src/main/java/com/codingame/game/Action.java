package com.codingame.game;

import com.codingame.game.core.Owner;

/**
 * An action that was parsed from the player's output.
 */
public class Action {
	
	public static final int NO_SELECTION = -1;
	
	public static enum Type {
		
		PICK, //
		DEPLOY, //
		MOVE, //
		RANDOM, //
		WAIT; //
		
		@Override
		public String toString() {
			return name();
		}
	}
	
	private Type type;
	private int targetId; // the id of the target field (used in all move types)
	private int sourceId; // the id of the source field (only for MOVEMENT)
	private int numTroops; // the number of troops that are deployed / moved (must be greater than 0)
	
	private Owner owner;
	
	public Action(Type type) {
		this(type, NO_SELECTION, NO_SELECTION, 0);
	}
	
	public Action(Type type, int targetId) {
		this(type, NO_SELECTION, targetId, 0);
	}
	
	public Action(Type type, int targetId, int numTroops) {
		this(type, NO_SELECTION, targetId, numTroops);
	}
	
	public Action(Type type, int sourceId, int targetId, int numTroops) {
		this.type = type;
		this.sourceId = sourceId;
		this.targetId = targetId;
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
	
	public Owner getOwner() {
		return owner;
	}
	
	public Action setOwner(Owner owner) {
		this.owner = owner;
		return this;
	}
	
	@Override
	public String toString() {
		switch (type) {
			case PICK:
				return String.format("%s %d", type, targetId);
			case DEPLOY:
				return String.format("%s %d %d", type, targetId, numTroops);
			case MOVE:
				return String.format("%s %d %d %d", type, sourceId, targetId, numTroops);
			case RANDOM:
			case WAIT:
				return String.format("%s", type);
			default:
				throw new IllegalStateException("Invalid action state");
		}
	}
}
