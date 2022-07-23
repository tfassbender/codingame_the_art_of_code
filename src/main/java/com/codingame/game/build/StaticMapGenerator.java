package com.codingame.game.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;

/*
 * Generates hard coded maps for development.
 */
public class StaticMapGenerator {
	
	/*
	 * Generates a map with 5 regions and 26 fields:
	 * 	- region A,D: 6 fields, bonus 7
	 *  - region C: 6 fields, bonus 5
	 *  - region B,E: 4 fields, bonus 3
	 */
	public GameMap createMapFiveRegions() {
		int nFields = 26;
		List<Field> fields = new ArrayList<Field>();
		Set<Region> regions = new HashSet<Region>();
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<>();
		List<Integer> regionCIds = new ArrayList<>();
		regionCIds.add(10);
		regionCIds.add(11);
		regionCIds.add(12);
		regionCIds.add(23);
		regionCIds.add(24);
		regionCIds.add(25);
		
		// create all fields
		for (int i = 0; i < nFields; i++) {
			int ntroops = 2;
			fields.add(Field.createNeutralCamp(i, ntroops));
		}
		
		// create all regions
		int bonusTroupsAD = 7;
		int bonusTroupsC = 5;
		int bonusTroupsBE = 3;
		regions.add(createRegion(0, 6, fields, bonusTroupsAD)); // A
		regions.add(createRegion(6, 10, fields, bonusTroupsBE)); // B
		regions.add(createRegion(regionCIds, fields, bonusTroupsC)); // C
		regions.add(createRegion(13, 19, fields, bonusTroupsAD)); // D
		regions.add(createRegion(19, 23, fields, bonusTroupsBE)); // E
		
		// create all connections
		
		// connections in region A
		connectionIds.addAll(connectCycle(0, 1, 2, 4, 5, 3));
		connectionIds.addAll(connectCycle(1, 4, 3));
		
		// connections in region B
		connectionIds.addAll(connectCycle(6, 8, 9, 7));
		connectionIds.add(Pair.of(7, 8));
		
		// connections in region C
		connectionIds.addAll(connectCycle(10, 23, 24, 25, 12, 11));
		connectionIds.add(Pair.of(11, 24));
		
		// connections in region D
		connectionIds.addAll(connectCycle(13, 14, 15, 17, 18, 16));
		connectionIds.addAll(connectCycle(14, 17, 16));
		
		// connections in region E
		connectionIds.addAll(connectCycle(19, 21, 22, 20));
		connectionIds.add(Pair.of(20, 21));
		
		// connections between regions
		connectionIds.add(Pair.of(0, 13)); // A <-> D
		connectionIds.add(Pair.of(4, 11)); // A <-> C
		connectionIds.add(Pair.of(24, 17)); // D <-> C
		connectionIds.add(Pair.of(2, 10)); // A <-> C
		connectionIds.add(Pair.of(5, 6)); // A <-> B
		connectionIds.add(Pair.of(8, 12)); // B <-> C
		connectionIds.add(Pair.of(15, 23)); // D <-> C
		connectionIds.add(Pair.of(21, 25)); // E <-> C
		connectionIds.add(Pair.of(18, 19)); // D <-> E
		
		return new GameMap(new HashSet<>(fields), createConnections(connectionIds, fields), regions);
	}
	
	/*
	 * Generates a map with 2 regions and 18:
	 * 	- region A,B: 9 fields, bonus 5
	 */
	public GameMap createMapTwoRegions() {
		int nFields = 18;
		List<Field> fields = new ArrayList<Field>();
		Set<Region> regions = new HashSet<Region>();
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<>();
		
		// create all fields
		for (int i = 0; i < nFields; i++) {
			int ntroops = 2;
			fields.add(Field.createNeutralCamp(i, ntroops));
		}
		
		// create all regions
		int bonusTroups = 5;
		regions.add(createRegion(0, nFields / 2, fields, bonusTroups)); // A
		regions.add(createRegion(nFields / 2, nFields, fields, bonusTroups)); // B
		
		// create all connections
		
		// connections in region A
		connectionIds.addAll(connectCycle(0, 1, 4, 6, 8, 7, 2));
		connectionIds.addAll(connectLine(2, 3, 4));
		connectionIds.addAll(connectLine(2, 5, 6));
		
		// connections in region B
		connectionIds.addAll(connectCycle(10, 9, 11, 16, 17, 15, 13));
		connectionIds.addAll(connectLine(11, 12, 13));
		connectionIds.addAll(connectLine(11, 14, 15));
		
		// connections between regions
		connectionIds.add(Pair.of(1, 10));
		connectionIds.add(Pair.of(8, 17));
		connectionIds.add(Pair.of(2, 11));
		
		return new GameMap(new HashSet<>(fields), createConnections(connectionIds, fields), regions);
	}
	
	/*
	 * Generates a map with 1 region and 8:
	 * 	- region A: 8 fields, bonus 1
	 */
	public GameMap createMapOneRegion() {
		int nFields = 8;
		List<Field> fields = new ArrayList<Field>();
		Set<Region> regions = new HashSet<Region>();
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<>();
		
		// create all fields
		for (int i = 0; i < nFields; i++) {
			int ntroops = 2;
			fields.add(Field.createNeutralCamp(i, ntroops));
		}
		
		// create all regions
		int bonusTroups = 1;
		regions.add(createRegion(0, nFields, fields, bonusTroups)); // A
		
		// create all connections
		connectionIds.addAll(connectCycle(0, 4, 5, 7, 3, 1));
		connectionIds.addAll(connectLine(1, 2, 6, 5));
		
		return new GameMap(new HashSet<>(fields), createConnections(connectionIds, fields), regions);
	}
	
	private List<Pair<Integer, Integer>> connectCycle(int... ids) {
		List<Pair<Integer, Integer>> connectionIds = connectLine(ids);
		
		connectionIds.add(Pair.of(ids[ids.length - 1], ids[0]));
		
		return connectionIds;
	}
	
	private List<Pair<Integer, Integer>> connectLine(int... ids) {
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<Pair<Integer, Integer>>();
		
		for (int i = 0; i < ids.length - 1; i++) {
			connectionIds.add(Pair.of(ids[i], ids[i + 1]));
		}
		
		return connectionIds;
	}
	
	private Region createRegion(int minId, int maxId, Collection<Field> fields, int bonusTroups) {
		return new Region(fields.stream().filter(f -> f.id >= minId && f.id < maxId).collect(Collectors.toSet()), bonusTroups);
	}
	
	private Region createRegion(List<Integer> selection, Collection<Field> fields, int bonusTroups) {
		return new Region(fields.stream().filter(f -> selection.contains(f.id)).collect(Collectors.toSet()), bonusTroups);
	}
	
	private Set<Pair<Field, Field>> createConnections(Collection<Pair<Integer, Integer>> connectIds, List<Field> fields) {
		return connectIds.stream().map(con -> Pair.of(fields.get(con.getKey()), fields.get(con.getValue()))).collect(Collectors.toSet());
	}
	
}
