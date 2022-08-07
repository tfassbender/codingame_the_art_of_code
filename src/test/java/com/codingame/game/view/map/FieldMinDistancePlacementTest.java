package com.codingame.game.view.map;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.codingame.game.core.Field;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.PositionedField;

class FieldMinDistancePlacementTest {
	
	@Test
	public void test_minDistanceReached() {
		PositionedField field1 = new PositionedField(new Field(1), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN / 2 + 10, FieldMinDistancePlacement.FIELD_Y_MAX / 2));
		PositionedField field2 = new PositionedField(new Field(2), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN / 2 + 50, FieldMinDistancePlacement.FIELD_Y_MAX / 2));
		
		Set<PositionedField> fields = new HashSet<>();
		fields.add(field1);
		fields.add(field2);
		
		FieldMinDistancePlacement.positionFields(fields);
		
		assertTrue(field1.pos().distance(field2.pos()) >= FieldMinDistancePlacement.MIN_DISTANCE_BETWEEN_FIELDS - 0.1f);
	}
	
	@Test
	public void test_movementInBounds_x_min() {
		PositionedField field1 = new PositionedField(new Field(1), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN + 10, FieldMinDistancePlacement.FIELD_Y_MAX / 2));
		PositionedField field2 = new PositionedField(new Field(2), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN + 50, FieldMinDistancePlacement.FIELD_Y_MAX / 2));
		
		Set<PositionedField> fields = new HashSet<>();
		fields.add(field1);
		fields.add(field2);
		
		FieldMinDistancePlacement.positionFields(fields);
		
		assertTrue(field1.pos().x >= FieldMinDistancePlacement.FIELD_X_MIN - 0.1f);
	}
	
	@Test
	public void test_movementInBounds_x_max() {
		PositionedField field1 = new PositionedField(new Field(1), new Vector2D(FieldMinDistancePlacement.FIELD_X_MAX - 10, FieldMinDistancePlacement.FIELD_Y_MAX / 2));
		PositionedField field2 = new PositionedField(new Field(2), new Vector2D(FieldMinDistancePlacement.FIELD_X_MAX - 50, FieldMinDistancePlacement.FIELD_Y_MAX / 2));
		
		Set<PositionedField> fields = new HashSet<>();
		fields.add(field1);
		fields.add(field2);
		
		FieldMinDistancePlacement.positionFields(fields);
		
		assertTrue(field1.pos().x <= FieldMinDistancePlacement.FIELD_X_MAX + 0.1f);
	}
	
	@Test
	public void test_movementInBounds_y_min() {
		PositionedField field1 = new PositionedField(new Field(1), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN / 2, FieldMinDistancePlacement.FIELD_Y_MIN + 10));
		PositionedField field2 = new PositionedField(new Field(2), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN / 2, FieldMinDistancePlacement.FIELD_Y_MIN + 50));
		
		Set<PositionedField> fields = new HashSet<>();
		fields.add(field1);
		fields.add(field2);
		
		FieldMinDistancePlacement.positionFields(fields);
		
		assertTrue(field1.pos().y >= FieldMinDistancePlacement.FIELD_Y_MIN - 0.1f);
	}
	
	@Test
	public void test_movementInBounds_y_max() {
		PositionedField field1 = new PositionedField(new Field(1), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN / 2, FieldMinDistancePlacement.FIELD_Y_MAX - 10));
		PositionedField field2 = new PositionedField(new Field(2), new Vector2D(FieldMinDistancePlacement.FIELD_X_MIN / 2, FieldMinDistancePlacement.FIELD_Y_MAX - 50));
		
		Set<PositionedField> fields = new HashSet<>();
		fields.add(field1);
		fields.add(field2);
		
		FieldMinDistancePlacement.positionFields(fields);
		
		assertTrue(field1.pos().y <= FieldMinDistancePlacement.FIELD_Y_MAX + 0.1f);
	}
}
