package com.codingame.game.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.Map;
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
	public Map createMapFiveRegions() {
		int nFields = 26;
		List<Field> fields = new ArrayList<Field>();
		Set<Region> regions = new HashSet<Region>();
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<>();
		List<Integer> idsForOneTroop = createListFromIds(1, 17, 5, 21, 9, 25);
		List<Integer> idsForThreeTroop = createListFromIds(4, 19, 12, 15);
		
		// create all fields
		for (int i = 0; i < nFields; i++) {
			int ntroops = 2;
			
			if (idsForOneTroop.contains(i)) ntroops = 1;
			else if (idsForThreeTroop.contains(i)) ntroops = 3;
			
			fields.add(Field.createNeutralCamp(i, ntroops));
		}
		
		// create all regions
		int bonusTroupsAD = 7;
		int bonusTroupsC = 5;
		int bonusTroupsBE = 3;
		regions.add(createRegion(0, 6, fields, bonusTroupsAD)); // A
		regions.add(createRegion(6, 10, fields, bonusTroupsBE)); // B
		regions.add(createRegion(10, 15, fields, bonusTroupsC)); // C
		regions.add(createRegion(16, 22, fields, bonusTroupsAD)); // D
		regions.add(createRegion(22, 26, fields, bonusTroupsBE)); // E
		
		// create all connections
		
		// connections in region A
		connectionIds.addAll(connectCycle(0, 1, 2, 4, 5, 3));
		connectionIds.addAll(connectCycle(1, 4, 3));
		
		// connections in region B
		connectionIds.addAll(connectCycle(6, 8, 9, 7));
		connectionIds.add(Pair.of(7, 8));
		
		// connections in region C
		connectionIds.addAll(connectCycle(10, 13, 14, 15, 12, 11));
		connectionIds.add(Pair.of(11, 14));
		
		// connections in region D
		connectionIds.addAll(connectCycle(16, 17, 18, 19, 20, 21));
		connectionIds.addAll(connectCycle(17, 20, 19));
		
		// connections in region E
		connectionIds.addAll(connectCycle(22, 24, 25, 23));
		connectionIds.add(Pair.of(23, 24));
		
		// connections between regions
		connectionIds.add(Pair.of(0, 18));
		connectionIds.add(Pair.of(4, 11));
		connectionIds.add(Pair.of(14, 19));
		connectionIds.add(Pair.of(2, 10));
		connectionIds.add(Pair.of(5, 6));
		connectionIds.add(Pair.of(8, 12));
		connectionIds.add(Pair.of(13, 16));
		connectionIds.add(Pair.of(15, 23));
		connectionIds.add(Pair.of(21, 22));
		
		return new Map(regions, new HashSet<>(fields), createConnections(connectionIds, fields));
	}
	
	/*
	 * Generates a map with 2 regions and 18:
	 * 	- region A,B: 9 fields, bonus 5
	 */
	public Map createMapTwoRegions() {
		int nFields = 18;
		List<Field> fields = new ArrayList<Field>();
		Set<Region> regions = new HashSet<Region>();
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<>();
		List<Integer> idsForOneTroop = createListFromIds(1, 2, 6, 9, 13, 14);
		List<Integer> idsForThreeTroop = createListFromIds(5, 15);
		
		// create all fields
		for (int i = 0; i < nFields; i++) {
			int ntroops = 2;
			
			if (idsForOneTroop.contains(i)) ntroops = 1;
			else if (idsForThreeTroop.contains(i)) ntroops = 3;
			
			fields.add(Field.createNeutralCamp(i, ntroops));
		}
		
		// create all regions
		int bonusTroups = 5;
		regions.add(createRegion(0, nFields/2, fields, bonusTroups)); // A
		regions.add(createRegion(nFields/2, nFields, fields, bonusTroups)); // B
		
		// create all connections
		
		// connections in region A
		connectionIds.addAll(connectCycle(0, 1, 4, 6, 8, 7, 2));
		connectionIds.addAll(connectLine(2, 3, 4));
		connectionIds.addAll(connectLine(2, 5, 6));
		
		// connections in region B
		connectionIds.addAll(connectCycle(9, 10, 13, 17, 16, 14, 11));
		connectionIds.addAll(connectLine(11, 12, 13));
		connectionIds.addAll(connectLine(14, 15, 13));
		
		// connections between regions
		connectionIds.add(Pair.of(1, 9));
		connectionIds.add(Pair.of(8, 16));
		connectionIds.add(Pair.of(2, 13));
		
		return new Map(regions, new HashSet<>(fields), createConnections(connectionIds, fields));
	}
	
	/*
	 * Generates a map with 1 region and 8:
	 * 	- region A: 8 fields, bonus 1
	 */
	public Map createMapOneRegion() {
		int nFields = 8;
		List<Field> fields = new ArrayList<Field>();
		Set<Region> regions = new HashSet<Region>();
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<>();
		List<Integer> idsForOneTroop = createListFromIds(0, 4);
		List<Integer> idsForThreeTroop = createListFromIds(3, 7);
		
		// create all fields
		for (int i = 0; i < nFields; i++) {
			int ntroops = 2;
			
			if (idsForOneTroop.contains(i)) ntroops = 1;
			else if (idsForThreeTroop.contains(i)) ntroops = 3;
			
			fields.add(Field.createNeutralCamp(i, ntroops));
		}
		
		// create all regions
		int bonusTroups = 1;
		regions.add(createRegion(0, nFields, fields, bonusTroups)); // A
		
		// create all connections
		connectionIds.addAll(connectCycle(0, 4, 6, 7, 3, 1));
		connectionIds.addAll(connectLine(1, 2, 5, 6));
		
		return new Map(regions, new HashSet<>(fields), createConnections(connectionIds, fields));
	}
	
	private List<Integer> createListFromIds(int... ids) {
		List<Integer> idsAsList = new ArrayList<Integer>();
		
		for (int id : ids) {
			idsAsList.add(id);
		}
		
		return idsAsList;
	}
	
	private List<Pair<Integer, Integer>> connectCycle(int... ids) {
		List<Pair<Integer, Integer>> connectionIds = connectLine(ids);
		
		connectionIds.add(Pair.of(ids[ids.length-1], ids[0]));
		
		return connectionIds;
	}
	
	private List<Pair<Integer, Integer>> connectLine(int... ids) {
		List<Pair<Integer, Integer>> connectionIds = new ArrayList<Pair<Integer, Integer>>();
		
		for (int i = 0; i < ids.length-1; i++) {
			connectionIds.add(Pair.of(ids[i], ids[i+1]));
		}
		
		return connectionIds;
	}
	
	private Region createRegion(int minId, int maxId, Collection<Field> fields, int bonusTroups) {
		return new Region(fields.stream().filter(f -> f.id >= minId && f.id < maxId).collect(Collectors.toSet()), bonusTroups);
	}
	
	private Set<Pair<Field, Field>> createConnections(Collection<Pair<Integer, Integer>> connectIds, List<Field> fields) {
		return connectIds.stream().map(con -> Pair.of(fields.get(con.getKey()), fields.get(con.getValue()))).collect(Collectors.toSet());
	}
	
}
