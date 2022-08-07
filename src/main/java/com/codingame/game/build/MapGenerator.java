package com.codingame.game.build;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.PositionedField;
import com.codingame.game.view.View;
import com.codingame.game.view.map.Cluster;
import com.codingame.game.view.map.ClusterAnalyzer;

/**
 * Generates a random map on which the game is played.
 */
public class MapGenerator {
	
	private static final float FIELD_WIDTH = View.GAME_FIELD_WIDTH - 100; // -100 so the fields are not pushed to the edge completely
	private static final float FIELD_HEIGHT = View.GAME_FIELD_HEIGHT - 100; // -100 so the fields are not pushed to the edge completely
	
	private static final int NUM_FIELDS_MIN = 16;
	private static final int NUM_FIELDS_MAX = 30;
	
	private static final int NUM_CONNECTIONS_PER_FIELD_MIN = 2;
	private static final int NUM_CONNECTIONS_PER_FIELD_MAX = 3; // can be more, if other fields connect to a field after it got processed or if groups need to be connected
	
	private static final int NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MIN = 1;
	private static final int NUM_CONNECTIONS_BETWEEN_DIVIDED_GROUPS_MAX = 2;
	
	private static final int NUM_CONNECTIONS_BETWEEN_SIDES_MIN = 2;
	
	private static final int NUM_REGIONS_MIN = 4;
	private static final int NUM_REGIONS_MAX = 8;
	
	private static final float MIN_DISTANCE_BETWEEN_REGION_CENTERS = 300;
	
	private static final float BONUS_TROOPS_PER_FIELD_IN_REGION = 0.67f; // 2 troop per three fields
	private static final int BONUS_TROOPS_RANDOM = 2;
	private static final int BONUS_TROOPS_MIN = 2;
	private static final int BONUS_TROOPS_MAX = 5;
	
	public static Pair<GameMap, Map<Field, Vector2D>> generateMap() {
		return new MapGenerator().generateRandomMap();
	}
	
	private int numFields;
	private Map<Field, Vector2D> positions;
	private RandomUtil random;
	
	private List<Field> fields;
	private Map<Field, Set<Field>> connections;
	private List<Region> regions;
	
	protected MapGenerator() {
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
	 * 3. Choose regions using an X-Means algorithm
	 * 4. Connect the fields randomly:
	 * - 4.1. For all pairs of fields: Chance to be connected higher for near fields and fields of the same region
	 * - 4.2. While the field can be divided into groups: Connect a random number of the closest fields of two groups
	 * - 4.3. For all fields, that have to many connections (more than allowed): Try to cut connections, but keep all fields connected transitively
	 * 5. Mirror the graph (so it's symmetric)
	 * 6. Create connections between the fields on both sides (where the chance to be connected is proportionately to the distance between the fields)
	 */
	private Pair<GameMap, Map<Field, Vector2D>> generateRandomMap() {
		chooseNumberOfFields();
		initializeHalfFields();
		positionFields();
		chooseRegions();
		
		connectFields();
		Optional<Set<Set<Field>>> dividedFieldGroups;
		while ((dividedFieldGroups = getDividedFieldGroups()).isPresent()) {
			connectGroups(dividedFieldGroups.get());
		}
		removeDispensableConnections();
		
		mirrorGraph();
		connectSides();
		
		return Pair.of(buildGameMap(), positions);
	}
	
	private void chooseNumberOfFields() {
		numFields = random.nextInt(NUM_FIELDS_MAX - NUM_FIELDS_MIN) + NUM_FIELDS_MIN;
		
		// ensure that the number of fields is even (so the map is symmetric)
		if (numFields % 2 == 1) {
			numFields++;
		}
	}
	
	private void initializeHalfFields() {
		for (int i = 0; i < numFields / 2; i++) {
			fields.add(new Field(i));
		}
	}
	
	private void positionFields() {
		final float halfWidth = FIELD_WIDTH / 2;
		
		for (Field field : fields) {
			positions.put(field, new Vector2D(random.nextFloat() * halfWidth, random.nextFloat() * FIELD_HEIGHT));
		}
	}
	
	private void chooseRegions() {
		List<PositionedField> positionedFields = fields.stream()//
				.map(field -> new PositionedField(field, positions.get(field))) //
				.collect(Collectors.toList());
		
		// divide the cluster into half the number of regions, because it is mirrored afterwards
		List<Cluster<PositionedField>> clusters = ClusterAnalyzer.getClusters(positionedFields, NUM_REGIONS_MIN / 2, NUM_REGIONS_MAX / 2, //
				MIN_DISTANCE_BETWEEN_REGION_CENTERS);
		
		for (Cluster<PositionedField> cluster : clusters) {
			Set<Field> fieldsInCluster = cluster.entries.stream() //
					.map(PositionedField::getField) //
					.collect(Collectors.toSet());
			
			Region region = new Region(fieldsInCluster, calculateBonusTroopsForRegion(fieldsInCluster));
			
			regions.add(region);
		}
	}
	
	private int calculateBonusTroopsForRegion(Set<Field> fieldsInCluster) {
		int bonusTroops = (int) (fieldsInCluster.size() * BONUS_TROOPS_PER_FIELD_IN_REGION) //
				+ random.nextInt(BONUS_TROOPS_RANDOM * 2) - BONUS_TROOPS_RANDOM;
		
		bonusTroops = Math.max(BONUS_TROOPS_MIN, Math.min(BONUS_TROOPS_MAX, bonusTroops));
		
		return bonusTroops;
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
					double regionFactor = (isFieldsInSameRegion(field, other) ? 1 : 0.5f);
					double connectionProbability = relativeDistance * regionFactor;
					
					if (random.nextFloat() > connectionProbability) { // chance to be connected higher for near fields and fields of the same region
						numConnections++;
						
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
					
					connectNearestFields(numConnectionsToAdd, possibleConnections);
				}
			}
		}
	}
	
	private void removeDispensableConnections() {
		boolean removedConnection = true;
		while (removedConnection) {
			removedConnection = false;
			
			// remove one connection from every field that has to much connections
			for (Field field : fields) {
				Set<Field> connectionsToCurrentField = new HashSet<>(connections.get(field)); // copy the set to prevent a concurrent modification
				if (connectionsToCurrentField.size() > NUM_CONNECTIONS_PER_FIELD_MAX) {
					for (Field other : connectionsToCurrentField) {
						removeConnection(field, other);
						if (getDividedFieldGroups().isPresent() || // check whether all fields are still connected transitively 
								connections.get(other).size() < NUM_CONNECTIONS_PER_FIELD_MIN) { // check whether the other field has not enough connections now
							// re-connect the fields (backtracking)
							connectFields(field, other);
						}
						else {
							removedConnection = true;
							// only remove one connection in every step
							break;
						}
					}
				}
			}
		}
	}
	
	private void mirrorGraph() {
		// mirror all fields
		Map<Field, Vector2D> mirroredPositions = new HashMap<>();
		int numHalfFields = numFields / 2;
		for (Field field : fields) {
			Vector2D position = positions.get(field);
			Vector2D mirroredPosition = new Vector2D(FIELD_WIDTH - position.x, position.y); // mirror on the right side of the map-rectangle
			Field mirroredField = new Field(numHalfFields + field.id);
			mirroredPositions.put(mirroredField, mirroredPosition);
		}
		fields.addAll(mirroredPositions.keySet());
		positions.putAll(mirroredPositions);
		
		// sort the fields by id, because the added mirrored fields came from an unsorted set
		fields.sort(Comparator.comparing(field -> field.id));
		
		// add connections between the mirrored fields
		Map<Field, Set<Field>> mirroredConnections = new HashMap<>();
		for (Entry<Field, Set<Field>> connectedFields : connections.entrySet()) {
			Field field1 = connectedFields.getKey();
			for (Field field2 : connectedFields.getValue()) {
				Field mirroredFieldId1 = fields.get(field1.id + numHalfFields);
				Field mirroredFieldId2 = fields.get(field2.id + numHalfFields);
				
				mirroredConnections.computeIfAbsent(mirroredFieldId1, x -> new HashSet<>()).add(mirroredFieldId2);
				mirroredConnections.computeIfAbsent(mirroredFieldId2, x -> new HashSet<>()).add(mirroredFieldId1);
			}
		}
		connections.putAll(mirroredConnections);
		
		// add the mirrored fields to new regions, so the regions are mirrored too
		List<Region> mirroredRegions = new ArrayList<>();
		for (Region region : regions) {
			Set<Field> mirroredRegionFields = region.fields.stream() //
					.map(field -> fields.get(field.id + numHalfFields)) //
					.collect(Collectors.toSet());
			
			mirroredRegions.add(new Region(mirroredRegionFields, region.bonusTroops));
		}
		regions.addAll(mirroredRegions);
	}
	
	private void connectSides() {
		final double maxDistance = FIELD_WIDTH;
		int numConnections = 0;
		
		for (int i = 0; i < numFields / 2; i++) {
			Field field = fields.get(i);
			Field mirrored = fields.get(i + numFields / 2);
			double distanceBetweenFields = positions.get(field).distance(positions.get(mirrored));
			double relativeDistance = distanceBetweenFields / maxDistance;
			double connectionProbability = relativeDistance * 3f; // reduce the probability to not connect to many fields
			
			if (random.nextFloat() > connectionProbability) { // chance to be connected is proportionately to the distance between the fields
				connectFields(field, mirrored);
				numConnections++;
			}
		}
		
		if (numConnections < NUM_CONNECTIONS_BETWEEN_SIDES_MIN) {
			Set<Pair<Field, Field>> possibleConnections = IntStream.range(0, numFields / 2) //
					.mapToObj(id -> Pair.of(fields.get(id), fields.get(id + numFields / 2))) //
					.collect(Collectors.toSet());
			
			connectNearestFields(NUM_CONNECTIONS_BETWEEN_SIDES_MIN, possibleConnections);
		}
	}
	
	public GameMap buildGameMap() {
		Set<Pair<Field, Field>> connectionSet = connections.entrySet().stream() //
				.flatMap(entry -> entry.getValue().stream().map(field2 -> Pair.of(entry.getKey(), field2))) //
				.collect(Collectors.toSet());
		
		//distinct the connection set, because the connections were added bidirectional
		Set<Pair<Field, Field>> distinctConnectionSet = new HashSet<>();
		for (Pair<Field, Field> connection : connectionSet) {
			if (!distinctConnectionSet.contains(connection) && !distinctConnectionSet.contains(connection.swapped())) {
				distinctConnectionSet.add(connection);
			}
		}
		
		return new GameMap(new HashSet<>(fields), distinctConnectionSet, new HashSet<>(regions));
	}
	
	//*************************************************************************
	//*** helper methods
	//*************************************************************************
	
	/**
	 * Add the connections bidirectional 
	 */
	private void connectFields(Field field1, Field field2) {
		connections.computeIfAbsent(field1, x -> new HashSet<>()).add(field2);
		connections.computeIfAbsent(field2, x -> new HashSet<>()).add(field1);
	}
	
	private boolean isFieldsConnected(Field field1, Field field2) {
		return connections.get(field1) != null && connections.get(field1).contains(field2);
	}
	
	private void connectNearestFields(int numConnectionsToAdd, Set<Pair<Field, Field>> possibleConnections) {
		possibleConnections.stream() //
				.sorted(Comparator.comparing(pair -> positions.get(pair.getKey()).distance(positions.get(pair.getValue())))) //
				.limit(numConnectionsToAdd) //
				.forEach(pair -> connectFields(pair.getKey(), pair.getValue()));
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
	
	private void removeConnection(Field field, Field other) {
		connections.get(field).remove(other);
		connections.get(other).remove(field);
	}
	
	private boolean isFieldsInSameRegion(Field field, Field other) {
		return regions.stream().anyMatch(region -> region.fields.contains(field) && region.fields.contains(other));
	}
}
