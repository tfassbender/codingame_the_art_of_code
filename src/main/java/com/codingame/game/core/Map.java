package com.codingame.game.core;

import java.util.Set;

import com.codingame.game.util.Pair;

/**
 * The game map, that contains all fields and troops.
 */
public class Map {
	
	public final Set<Field> fields;
	public final Set<Pair<Field, Field>> connections;
	public final Set<Region> regions;
	
	public Map(Set<Region> regions, Set<Field> fields, Set<Pair<Field, Field>> connections) {
		this.regions = regions;
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
