package com.codingame.game.view;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.map.Positioned;

public class PositionedField implements Positioned<Field> {
	
	public final Field field;
	
	private Vector2D position;
	
	public static Set<PositionedField> of(Map<Field, Vector2D> fieldPositions) {
		return fieldPositions.entrySet().stream() //
				.map(entry -> new PositionedField(entry.getKey(), entry.getValue())) //
				.collect(Collectors.toSet());
	}
	
	public PositionedField(Field field, Vector2D position) {
		this.field = field;
		this.position = position;
	}
	
	@Override
	public Vector2D pos() {
		return position;
	}
	
	@Override
	public void setPosition(Vector2D position) {
		this.position = position;
	}
	
	public Field getField() {
		return field;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(field);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PositionedField other = (PositionedField) obj;
		return Objects.equals(field, other.field);
	}
	
	@Override
	public String toString() {
		return "PositionedField [field=" + field + ", position=" + position + "]";
	}
}
