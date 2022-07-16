package com.codingame.game.core;

import java.util.Set;

import com.codingame.game.util.Pair;

/**
 * The game map, that contains all fields and troops.
 */
public class GameMap {
	
	public final Set<Field> fields;
	public final Set<Pair<Field, Field>> connections;
	public final Set<Region> regions;
	
	public GameMap(Set<Field> fields, Set<Pair<Field, Field>> connections, Set<Region> regions) {
		this.fields = fields;
		this.connections = connections;
		this.regions = regions;
	}
	
	public void execute(Movement movement) {
		//TODO
	}
	
	public int calculateDeployableTroops(Owner player) {
		//TODO
		return 0;
	}
}
