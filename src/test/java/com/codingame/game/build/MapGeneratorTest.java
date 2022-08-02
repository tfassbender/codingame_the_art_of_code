package com.codingame.game.build;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
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
import com.codingame.game.util.TestUtils;

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
		List<Field> fields = IntStream.range(0, 5).mapToObj(Field::new).collect(Collectors.toList());
		
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(0), fields.get(1));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(1), fields.get(2));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(1), fields.get(3));
		TestUtils.invokePrivateMethod(generator, "connectFields", fields.get(4), fields.get(3));
		
		TestUtils.setFieldPerReflection(generator, "fields", fields);
		
		@SuppressWarnings("unchecked")
		Optional<Set<Set<Field>>> dividedFields = (Optional<Set<Set<Field>>>) TestUtils.invokePrivateMethod(generator, "getDividedFieldGroups");
		
		assertFalse(dividedFields.isPresent());
	}
	
	//TODO test connectGroups
	//TODO test chooseRegions
	//TODO test calculateBonusTroopsForRegion
	//TODO test mirrorGraph
	//TODO test connectSides
	//TODO test buildGameMap
	
	private void randomizeSeed() {
		RandomUtil.init(new Random().nextLong());
	}
	
	private List<Field> getFields() throws Exception {
		return TestUtils.getFieldPerReflection(generator, "fields");
	}
	
	private Map<Field, Set<Field>> getConnections() throws Exception {
		return TestUtils.getFieldPerReflection(generator, "connections");
	}
}
