package com.codingame.game.core;

import java.util.Optional;
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
		
		// TODO if not all troops are deployed in a turn, they are added the the number of troops of the next turn
		return 0;
	}
	
	public Optional<Field> getFieldById(int id) {
		return fields.stream().filter(field -> field.id == id).findFirst();
	}
	
	public boolean isFieldsConnected(int sourceId, int targetId) {
		return connections.stream().anyMatch(pair -> pair.getKey().id == sourceId && pair.getValue().id == targetId || //
				pair.getKey().id == targetId && pair.getValue().id == sourceId);
	}
}
