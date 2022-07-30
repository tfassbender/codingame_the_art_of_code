package com.codingame.game.view;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.util.Vector2D;
import com.codingame.game.view.map.Graph;

public class MapGraph implements Graph<PositionedField> {
	
	public GameMap gameMap;
	public Set<PositionedField> fields;
	
	public MapGraph(GameMap gameMap, Map<Field, Vector2D> positions) {
		this.gameMap = gameMap;
		this.fields = gameMap.fields.stream().map(field -> new PositionedField(field, positions.get(field))).collect(Collectors.toSet());
	}
	
	@Override
	public Set<PositionedField> getFields() {
		return fields;
	}
	
	@Override
	public boolean isFieldsConnected(PositionedField field1, PositionedField field2) {
		return gameMap.isFieldsConnected(field1.field.id, field2.field.id);
	}
	
	@Override
	public boolean isFieldsInSameCluster(PositionedField field1, PositionedField field2) {
		return gameMap.getRegionForFieldById(field1.field.id) == gameMap.getRegionForFieldById(field2.field.id);
	}
}
