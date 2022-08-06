package com.codingame.game.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.codingame.game.core.Field;
import com.codingame.game.core.Region;
import com.codingame.game.util.Pair;
import com.codingame.game.util.TestUtils;
import com.codingame.game.util.Vector2D;

public class MapGeneratorTest {
	
	private MapGenerator generator;
	
	@BeforeEach
	public void setup() {
		RandomUtil.init(42);
		generator = new MapGenerator();
	}
	
	@RepeatedTest(10)
	public void test_chooseNumberOfFields() throws Throwable {
		randomizeSeed();
		
		TestUtils.invokePrivateMethod(generator, "chooseNumberOfFields");
		TestUtils.invokePrivateMethod(generator, "initializeHalfFields");
		int numFields = TestUtils.getFieldPerReflection(generator, "numFields");
		List<Field> fields = getFields();
		
		int numFieldsMin = TestUtils.getFieldPerReflection(generator, "NUM_FIELDS_MIN");
		int numFieldsMax = TestUtils.getFieldPerReflection(generator, "NUM_FIELDS_MAX");
		
		assertTrue(numFields >= numFieldsMin);
		assertTrue(numFields <= numFieldsMax);
		assertTrue(numFields % 2 == 0);
		assertEquals(numFields / 2, fields.size());
	}
	
	@RepeatedTest(10)
	public void test_connectFields_checkNumberOfConnectionsPerField() throws Throwable {
		randomizeSeed();
		
		TestUtils.invokePrivateMethod(generator, "chooseNumberOfFields");
		TestUtils.invokePrivateMethod(generator, "initializeHalfFields");
		TestUtils.invokePrivateMethod(generator, "positionFields");
		TestUtils.invokePrivateMethod(generator, "connectFields");
		
		List<Field> fields = getFields();
		Map<Field, Set<Field>> connections = getConnections();
		int numConnectionsPerFieldMin = TestUtils.getFieldPerReflection(generator, "NUM_CONNECTIONS_PER_FIELD_MIN");
		
		assertFalse(fields.isEmpty());
		
		for (Field field : fields) {
			assertTrue(connections.get(field).size() >= numConnectionsPerFieldMin);
		}
	}
	
	@Test
	public void test_getDividedFieldGroups_twoFields() throws Throwable {
		List<Field> fields = new ArrayList<>();
		fields.add(new Field(0));
		fields.add(new Field(1));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		
		@SuppressWarnings("unchecked")
		Optional<Set<Set<Field>>> dividedFields = (Optional<Set<Set<Field>>>) TestUtils.invokePrivateMethod(generator, "getDividedFieldGroups");
		
		assertTrue(dividedFields.isPresent());
		assertEquals(2, dividedFields.get().size());
		
		List<Set<Field>> sets = new ArrayList<>(dividedFields.get());
		Set<Field> set1 = sets.get(0);
		Set<Field> set2 = sets.get(1);
		
		assertEquals(1, set1.size());
		assertEquals(1, set2.size());
		assertNotEquals(set1.stream().findFirst().get(), set2.stream().findFirst().get());
	}
	
	@Test
	public void test_getDividedFieldGroups_threeFieldGroups() throws Throwable {
		List<Field> fields = IntStream.range(0, 6).mapToObj(Field::new).collect(Collectors.toList());
		
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(1));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(2), fields.get(3));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(4), fields.get(5));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		
		@SuppressWarnings("unchecked")
		Optional<Set<Set<Field>>> dividedFields = (Optional<Set<Set<Field>>>) TestUtils.invokePrivateMethod(generator, "getDividedFieldGroups");
		
		assertTrue(dividedFields.isPresent());
		assertEquals(3, dividedFields.get().size());
		
		List<Set<Field>> sets = new ArrayList<>(dividedFields.get());
		Set<Field> set1 = sets.get(0);
		Set<Field> set2 = sets.get(1);
		Set<Field> set3 = sets.get(2);
		
		assertEquals(2, set1.size());
		assertEquals(2, set2.size());
		assertEquals(2, set3.size());
		assertTrue(set1.stream().noneMatch(field -> set2.contains(field)));
		assertTrue(set1.stream().noneMatch(field -> set3.contains(field)));
		assertTrue(set2.stream().noneMatch(field -> set1.contains(field)));
		assertTrue(set2.stream().noneMatch(field -> set3.contains(field)));
		assertTrue(set3.stream().noneMatch(field -> set1.contains(field)));
		assertTrue(set3.stream().noneMatch(field -> set2.contains(field)));
	}
	
	@Test
	public void test_getDividedFieldGroups_notDivided() throws Throwable {
		List<Field> fields = createFields(5);
		
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(1));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(1), fields.get(2));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(1), fields.get(3));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(4), fields.get(3));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		
		@SuppressWarnings("unchecked")
		Optional<Set<Set<Field>>> dividedFields = (Optional<Set<Set<Field>>>) TestUtils.invokePrivateMethod(generator, "getDividedFieldGroups");
		
		assertFalse(dividedFields.isPresent());
	}
	
	@Test
	public void test_connectGroups_closestFieldsAreConnected() throws Throwable {
		List<Field> fields = createFields(5);
		Set<Field> group1 = new HashSet<>(fields.subList(0, 2));
		Set<Field> group2 = new HashSet<>(fields.subList(2, 5));
		Set<Set<Field>> groups = new HashSet<Set<Field>>();
		groups.add(group1);
		groups.add(group2);
		Map<Field, Vector2D> positions = fields.stream() //
				.map(field -> Pair.of(field, new Vector2D(field.id, 0))) // position fields in a line (by increasing id)
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		TestUtils.setFieldPerReflection(generator, "positions", positions);
		
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(1));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(2), fields.get(3));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(3), fields.get(4));
		
		TestUtils.invokePrivateMethod(generator, "connectGroups", new Class[] {Set.class}, groups);
		Map<Field, Set<Field>> connections = TestUtils.getFieldPerReflection(generator, "connections");
		
		assertTrue(allFieldsConnectedTransitively());
		assertTrue(connections.get(fields.get(1)).contains(fields.get(2)));
	}
	
	@Test
	public void test_connectGroups_allFieldsConnected() throws Throwable {
		List<Field> fields = createFields(5);
		Set<Set<Field>> groups = fields.stream() //
				.map(field -> {
					Set<Field> group = new HashSet<Field>();
					group.add(field);
					return group;
				}) //
				.collect(Collectors.toSet());
		Map<Field, Vector2D> positions = fields.stream() //
				.map(field -> Pair.of(field, new Vector2D(field.id, 0))) // position fields in a line (by increasing id)
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		TestUtils.setFieldPerReflection(generator, "positions", positions);
		
		TestUtils.invokePrivateMethod(generator, "connectGroups", new Class[] {Set.class}, groups);
		
		assertTrue(allFieldsConnectedTransitively());
	}
	
	@Test
	public void test_chooseRegions() throws Throwable {
		List<Field> fields = createFields(10);
		
		// position fields three groups on the x axis and increasing by their id on y axis
		Map<Field, Vector2D> positions = fields.stream() //
				.map(field -> Pair.of(field, new Vector2D((field.id % 3) * 1000, field.id))) // *1000 because of the min distance between cluster centers
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		TestUtils.setFieldPerReflection(generator, "positions", positions);
		
		TestUtils.invokePrivateMethod(generator, "chooseRegions");
		
		List<Region> regions = TestUtils.getFieldPerReflection(generator, "regions");
		
		Region region1 = regions.stream().filter(region -> region.fields.contains(fields.get(0))).findFirst().get(); // left most
		Region region2 = regions.stream().filter(region -> region.fields.contains(fields.get(1))).findFirst().get(); // middle
		Region region3 = regions.stream().filter(region -> region.fields.contains(fields.get(2))).findFirst().get(); // right most
		
		assertEquals(3, regions.size());
		assertEquals(4, region1.fields.size());
		assertEquals(3, region2.fields.size());
		assertEquals(3, region3.fields.size());
		
		assertTrue(region1.fields.contains(fields.get(3)));
		assertTrue(region1.fields.contains(fields.get(6)));
		assertTrue(region1.fields.contains(fields.get(9)));
		
		assertTrue(region2.fields.contains(fields.get(4)));
		assertTrue(region2.fields.contains(fields.get(7)));
		
		assertTrue(region3.fields.contains(fields.get(5)));
		assertTrue(region3.fields.contains(fields.get(8)));
	}
	
	@RepeatedTest(10)
	public void test_chooseRegions_randomized() throws Throwable {
		List<Field> fields = createFields(10);
		
		int numRegionsMin = TestUtils.getFieldPerReflection(generator, "NUM_REGIONS_MIN");
		int numRegionsMax = TestUtils.getFieldPerReflection(generator, "NUM_REGIONS_MAX");
		int numRegions = new Random().nextInt(numRegionsMax / 2 - numRegionsMin / 2) + numRegionsMin / 2; // /2 because only half the field is generated
		
		// fixed center positions around which the fields are positioned
		List<Vector2D> centers = Arrays.asList(new Vector2D(0, 0), new Vector2D(1000, 1000), new Vector2D(2000, 4000), new Vector2D(0, 5000));
		
		// position fields in groups around the fixed centers (with some random noise)
		Map<Field, Vector2D> positions = fields.stream() //
				.map(field -> Pair.of(field, addRandomNoise(centers.get(field.id % numRegions)))) //
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		TestUtils.setFieldPerReflection(generator, "positions", positions);
		
		TestUtils.invokePrivateMethod(generator, "chooseRegions");
		
		List<Region> regions = TestUtils.getFieldPerReflection(generator, "regions");
		
		assertEquals(numRegions, regions.size());
	}
	
	@Test
	public void test_mirrorGraph() throws Throwable {
		List<Field> fields = createFields(5);
		Map<Field, Vector2D> positions = fields.stream() //
				.map(field -> Pair.of(field, new Vector2D(field.id, 0))) // position fields in a line (by increasing id)
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		List<Region> regions = fields.stream() //
				.collect(Collectors.groupingBy(field -> field.id % 2)) //
				.values().stream() //
				.map(fieldList -> new Region(new HashSet<>(fieldList), 0)) //
				.collect(Collectors.toList());
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		TestUtils.setFieldPerReflection(generator, "numFields", fields.size() * 2);
		TestUtils.setFieldPerReflection(generator, "positions", positions);
		TestUtils.setFieldPerReflection(generator, "regions", regions);
		
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(1));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(2));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(1), fields.get(3));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(4), fields.get(3));
		
		TestUtils.invokePrivateMethod(generator, "mirrorGraph");
		Map<Field, Set<Field>> connections = getConnections();
		
		float fieldWidth = TestUtils.getFieldPerReflection(generator, "FIELD_WIDTH");
		
		Region region3 = regions.stream().filter(region -> region.fields.contains(fields.get(5))).findFirst().get();
		Region region4 = regions.stream().filter(region -> region.fields.contains(fields.get(6))).findFirst().get();
		
		assertEquals(10, fields.size());
		assertEquals(4, regions.size());
		assertTrue(isFieldsConnected(fields, connections, 5, 6));
		assertTrue(isFieldsConnected(fields, connections, 5, 7));
		assertTrue(isFieldsConnected(fields, connections, 6, 8));
		assertTrue(isFieldsConnected(fields, connections, 8, 9));
		for (int i = 5; i < fields.size(); i++) {
			assertEquals(fieldWidth - i + 5, positions.get(fields.get(i)).x, 1e-3);
			assertEquals(0, positions.get(fields.get(i)).y, 1e-3);
		}
		assertEquals(3, region3.fields.size());
		assertEquals(2, region4.fields.size());
		assertTrue(region3.fields.contains(fields.get(7)));
		assertTrue(region3.fields.contains(fields.get(9)));
		assertTrue(region4.fields.contains(fields.get(8)));
	}
	
	@Test
	public void test_connectSides() throws Throwable {
		List<Field> fields = createFields(5);
		Map<Field, Vector2D> positions = fields.stream() //
				.map(field -> Pair.of(field, new Vector2D(field.id, 0))) // position fields in a line (by increasing id)
				.collect(Collectors.toMap(Pair::getKey, Pair::getValue));
		List<Region> regions = fields.stream() //
				.collect(Collectors.groupingBy(field -> field.id % 2)) //
				.values().stream() //
				.map(fieldList -> new Region(new HashSet<>(fieldList), 0)) //
				.collect(Collectors.toList());
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		TestUtils.setFieldPerReflection(generator, "numFields", fields.size() * 2);
		TestUtils.setFieldPerReflection(generator, "positions", positions);
		TestUtils.setFieldPerReflection(generator, "regions", regions);
		
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(1));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(2));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(1), fields.get(3));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(4), fields.get(3));
		
		TestUtils.invokePrivateMethod(generator, "mirrorGraph");
		TestUtils.invokePrivateMethod(generator, "connectSides");
		
		assertEquals(10, fields.size());
		assertTrue(allFieldsConnectedTransitively());
	}
	
	//*****************************************************************************
	//*** helper methods
	//*****************************************************************************
	
	private void randomizeSeed() {
		RandomUtil.init(new Random().nextLong());
	}
	
	private List<Field> createFields(int numFields) {
		return IntStream.range(0, numFields).mapToObj(Field::new).collect(Collectors.toList());
	}
	
	private List<Field> getFields() throws Exception {
		return TestUtils.getFieldPerReflection(generator, "fields");
	}
	
	private Map<Field, Set<Field>> getConnections() throws Exception {
		return TestUtils.getFieldPerReflection(generator, "connections");
	}
	
	private boolean allFieldsConnectedTransitively() throws Throwable {
		@SuppressWarnings("unchecked")
		Optional<Set<Set<Field>>> dividedFields = (Optional<Set<Set<Field>>>) TestUtils.invokePrivateMethod(generator, "getDividedFieldGroups");
		return !dividedFields.isPresent();
	}
	
	private Vector2D addRandomNoise(Vector2D position) {
		return position.add(new Vector2D((Math.random() - 0.5) * 100, (Math.random() - 0.5) * 100));
	}
	
	private boolean isFieldsConnected(List<Field> fields, Map<Field, Set<Field>> connections, int fieldId1, int fieldId2) {
		return connections.get(getFieldById(fields, fieldId1)).contains(getFieldById(fields, fieldId2));
	}
	
	private Field getFieldById(List<Field> fields, int id) {
		return fields.stream().filter(field -> field.id == id).findFirst().get();
	}
}
