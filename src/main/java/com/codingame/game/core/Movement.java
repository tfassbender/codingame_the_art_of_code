package com.codingame.game.core;

/**
 * A planed movement of troops from one field to the next that can be executed on the map.
 */
public class Movement {
	
	public final Field source;
	public final Field target;
	public final int numTroops;
	
	public Movement(Field source, Field target, int numTroops) {
		this.source = source;
		this.target = target;
		this.numTroops = numTroops;
	}
}
