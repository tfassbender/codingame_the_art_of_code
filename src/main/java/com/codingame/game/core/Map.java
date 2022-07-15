package com.codingame.game.core;

import java.util.Set;

import com.codingame.game.util.Pair;

/**
 * The game map, that contains all fields and troops.
 */
public class Map {
	
	public final Set<Field> fields;
	public final Set<Pair<Field, Field>> connections;
	
	public Map(Set<Field> fields, Set<Pair<Field, Field>> connections) {
		this.fields = fields;
		this.connections = connections;
	}
	
	public void execute(Movement movement) {
		//TODO
	}
	
	public int calculateDeployableTroops(Owner player) {
		//TODO
		return 0;
	}
}
