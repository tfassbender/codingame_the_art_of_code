package com.codingame.game.build;

import java.util.HashSet;
import java.util.Set;

import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;

/**
 * Generates a random map on which the game is played.
 */
public class MapGenerator {
	
	/**
	 * TODO replace with a real map generator (or a better testing map)
	 */
	public GameMap createDummyMap() {
		Field field1 = new Field(0);
		Field field2 = new Field(1);
		Set<Field> fields = new HashSet<>();
		fields.add(field1);
		fields.add(field2);
		
		Set<Pair<Field, Field>> connections = new HashSet<>();
		connections.add(Pair.of(field1, field2));
		
		Set<Region> regions = new HashSet<>();
		Set<Field> region1 = new HashSet<>();
		Set<Field> region2 = new HashSet<>();
		region1.add(field1);
		region2.add(field2);
		regions.add(new Region(region1, 1));
		regions.add(new Region(region2, 1));
		
		return new GameMap(fields, connections, regions);
	}
}
