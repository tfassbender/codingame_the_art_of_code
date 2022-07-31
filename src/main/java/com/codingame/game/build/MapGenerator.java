package com.codingame.game.build;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.View;

/**
 * Generates a random map on which the game is played.
 */
public class MapGenerator {
	
	public static final int NUM_FIELDS_MIN = 10;
	public static final int NUM_FIELDS_MAX = 30;
	
	private static final int NUM_CONNECTIONS_PER_FIELD_MIN = 2;
	private static final int NUM_CONNECTIONS_PER_FIELD_MAX = 4; // can be more, if other fields connect to a field after it got processed or if groups need to be connected
	
	private static final int NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MIN = 1;
	private static final int NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MAX = 2;
	
	private static final float FIELD_WIDTH = View.GAME_FIELD_WIDTH - 100; // -100 so the fields are not pushed to the edge completely
	private static final float FIELD_HEIGHT = View.GAME_FIELD_HEIGHT - 100; // -100 so the fields are not pushed to the edge completely
	
	public static GameMap generateMap() {
		// until we can generate maps by our self we use random static
		// return selectRandomStaticMap();
		
		return new MapGenerator().generateRandomMap();
	}
	
	private static GameMap selectRandomStaticMap() {
		int selection = RandomUtil.getInstance().nextInt(3);
		StaticMapGenerator staticMaps = new StaticMapGenerator();
		
		if (selection == 0) {
			return staticMaps.createMapOneRegion();
		}
		else if (selection == 1) {
			return staticMaps.createMapTwoRegions();
		}
		else {
			return staticMaps.createMapFiveRegions();
		}
	}
	
	private int numFields;
	private Map<Field, Vector2D> positions;
	private RandomUtil random;
	
	private List<Field> fields;
	private Map<Field, Set<Field>> connections;
	private List<Region> regions;
	
	private MapGenerator() {
		random = RandomUtil.getInstance();
		positions = new HashMap<>();
		
		fields = new ArrayList<>();
		connections = new HashMap<>();
		regions = new ArrayList<>();
	}
	
	//*************************************************************************
	//*** random field generator algorithm
	//*************************************************************************
	
	/**
	 * Generates a random map by applying the following algorithm:
	 * 
	 * 1. Choose the number of fields (randomly)
	 * 2. Position half of the fields on one half of the map area (randomly)
	 * 3. Connect the fields randomly:
	 * - 3.1. For all pairs of fields: Chance to be connected is proportionately to the distance between the fields
	 * - 3.2. While the field can be divided into groups: Connect a random number of the closest fields of two groups
	 * 4. Choose regions using an X-Means algorithm
	 * 5. Mirror the graph (so it's symmetric)
	 * 6. Create connections between the fields on both sides (where the chance to be connected is proportionately to the distance between the fields)
	 */
	private GameMap generateRandomMap() {
		chooseNumberOfFields();
		positionFields();
		connectFields();
		
		Optional<Set<Set<Field>>> dividedFieldGroups;
		while ((dividedFieldGroups = getDividedFieldGroups()).isPresent()) {
			connectGroups(dividedFieldGroups.get());
		}
		
		//TODO step 4
		
		return null;
	}
	
	private void chooseNumberOfFields() {
		numFields = random.nextInt(NUM_FIELDS_MAX - NUM_FIELDS_MIN) + NUM_FIELDS_MIN;
		for (int i = 0; i < numFields; i++) {
			fields.add(new Field(i));
		}
		
		// ensure that the number of fields is even (so the map is symmetric)
		if (numFields % 2 == 1) {
			numFields++;
		}
	}
	
	private void positionFields() {
		final float halfWidth = FIELD_WIDTH / 2;
		
		for (Field field : fields) {
			positions.put(field, new Vector2D(random.nextFloat() * halfWidth, random.nextFloat() * FIELD_HEIGHT));
		}
	}
	
	private void connectFields() {
		final double maxDistance = Math.hypot(FIELD_WIDTH / 2, FIELD_HEIGHT);
		
		for (Field field : fields) {
			int numTargetConnections = random.nextInt(NUM_CONNECTIONS_PER_FIELD_MAX - NUM_CONNECTIONS_PER_FIELD_MIN) + NUM_CONNECTIONS_PER_FIELD_MIN;
			int numConnections = connections.computeIfAbsent(field, x -> new HashSet<>()).size();
			
			for (Field other : fields) {
				if (field != other) {
					double distanceBetweenFields = positions.get(field).distance(positions.get(other));
					double relativeDistance = distanceBetweenFields / maxDistance;
					
					if (random.nextFloat() > relativeDistance) { // chance to be connected is proportionately to the distance between the fields
						numConnections++;
						
						// add the connections bidirectional
						connectFields(field, other);
						
						if (numConnections == numTargetConnections) {
							break;
						}
					}
				}
			}
			
		}
		
		// add connections to all fields that have not reached the minimum number of connections yet
		for (Field field : fields) {
			int numConnections = connections.computeIfAbsent(field, x -> new HashSet<>()).size();
			
			if (numConnections < NUM_CONNECTIONS_PER_FIELD_MIN) {
				// not enough connections were chosen for this field -> connect to the nearest fields till the minimum is reached
				fields.stream() //
						.filter(other -> other != field) //
						.filter(other -> !isFieldsConnected(field, other)) //
						.sorted(Comparator.comparing(other -> positions.get(field).distance(positions.get(other)))) //
						.limit(NUM_CONNECTIONS_PER_FIELD_MIN - numConnections) //
						.forEach(other -> connectFields(field, other));
			}
		}
	}
	
	/**
	 * Try to find groups of fields, that are not connected to each other and return them. 
	 * Or return an empty {@link Optional} if all fields are connected to each other (transitively).
	 */
	private Optional<Set<Set<Field>>> getDividedFieldGroups() {
		Set<Field> allFields = new HashSet<>(fields); // create a copy of the set, because elements will be removed from it
		Set<Set<Field>> fieldGroups = new HashSet<>();
		
		while (!allFields.isEmpty()) {
			// perform a depth first search, add all fields to the group and remove them from the allFields set
			Set<Field> group = new HashSet<>();
			
			Field start = allFields.stream().findFirst().get();
			performDepthFirstSearch(allFields, group, start);
			
			fieldGroups.add(group);
		}
		
		if (fieldGroups.size() == 1) {
			// all fields are connected transitively, so there are no divided field groups -> return an empty Optional
			return Optional.empty();
		}
		
		return Optional.of(fieldGroups);
	}
	
	private void performDepthFirstSearch(Set<Field> allFields, Set<Field> group, Field field) {
		allFields.remove(field);
		group.add(field);
		
		Set<Field> connectedFields = connections.computeIfAbsent(field, x -> new HashSet<>());
		for (Field connected : connectedFields) {
			if (!group.contains(connected)) {
				performDepthFirstSearch(allFields, group, connected);
			}
		}
	}
	
	/**
	 * Connect the field of the divided groups, so all fields are connected to each other (transitively).
	 */
	private void connectGroups(Set<Set<Field>> dividedFieldGroups) {
		// for each two of the divided field groups ...
		for (Set<Field> group1 : dividedFieldGroups) {
			for (Set<Field> group2 : dividedFieldGroups) {
				if (group1 != group2) {
					int numConnectionsToAdd = random.nextInt(NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MAX - NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MIN) + //
							NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MIN;
					
					// find all possible connections between fields of both groups
					Set<Pair<Field, Field>> possibleConnections = group1.stream() //
							.flatMap(field1 -> group2.stream().map(field2 -> Pair.of(field1, field2))) //
							.collect(Collectors.toSet());
					
					// connect the nearest fields of the groups
					possibleConnections.stream() //
							.sorted(Comparator.comparing(pair -> positions.get(pair.getKey()).distance(positions.get(pair.getValue())))) //
							.limit(numConnectionsToAdd) //
							.forEach(pair -> connectFields(pair.getKey(), pair.getValue()));
				}
			}
		}
	}
	
	//*************************************************************************
	//*** helper methods
	//*************************************************************************
	
	private void connectFields(Field field1, Field field2) {
		connections.computeIfAbsent(field1, x -> new HashSet<>()).add(field2);
		connections.computeIfAbsent(field2, x -> new HashSet<>()).add(field1);
	}
	
	private boolean isFieldsConnected(Field field1, Field field2) {
		return connections.get(field1) != null && connections.get(field1).contains(field2);
	}
}
