package com.codingame.game.view.map;

import java.util.Set;

import com.codingame.game.util.Vector2D;
import com.codingame.game.view.PositionedField;
import com.codingame.game.view.View;

/**
 * Iterates over the positioned fields after the {@link GraphPlacement} to ensure that the minimum distance between the fields is kept.
 */
public class FieldMinDistancePlacement {
	
	private static final int MAX_ITERATIONS = 100;
	public static final float MIN_DISTANCE_BETWEEN_FIELDS = 125f;
	
	public static final float FIELD_X_MIN = 50;
	public static final float FIELD_X_MAX = View.GAME_FIELD_WIDTH - 100;
	public static final float FIELD_Y_MIN = 50;
	public static final float FIELD_Y_MAX = View.GAME_FIELD_HEIGHT - 100;
	
	private FieldMinDistancePlacement() {}
	
	public static Set<PositionedField> positionFields(Set<PositionedField> fields) {
		int iteration = 0;
		boolean fieldsMoved = true;
		
		while (fieldsMoved && iteration < MAX_ITERATIONS) {
			fieldsMoved = false;
			
			for (PositionedField field : fields) {
				for (PositionedField other : fields) {
					if (field != other && field.pos().distance(other.pos()) < MIN_DISTANCE_BETWEEN_FIELDS) {
						Vector2D fromFieldToOther = field.pos().vectorTo(other.pos());
						double distance = field.pos().distance(other.pos());
						double additionalSpaceNeeded = MIN_DISTANCE_BETWEEN_FIELDS - distance + 0.1f; // +0.1f to not move the same fields again, because of rounding errors
						
						// move both fields away from each other
						field.setPosition(field.pos().sub(fromFieldToOther.setLength(additionalSpaceNeeded / 2)));
						other.setPosition(other.pos().add(fromFieldToOther.setLength(additionalSpaceNeeded / 2)));
						
						moveFieldIntoBounds(field);
						moveFieldIntoBounds(other);
						
						fieldsMoved = true;
					}
				}
			}
		}
		
		return fields;
	}
	
	private static void moveFieldIntoBounds(PositionedField field) {
		if (field.pos().x < FIELD_X_MIN) {
			field.pos().x = FIELD_X_MIN;
		}
		if (field.pos().x > FIELD_X_MAX) {
			field.pos().x = FIELD_X_MAX;
		}
		if (field.pos().y < FIELD_Y_MIN) {
			field.pos().y = FIELD_Y_MIN;
		}
		if (field.pos().y > FIELD_Y_MAX) {
			field.pos().y = FIELD_Y_MAX;
		}
	}
}
