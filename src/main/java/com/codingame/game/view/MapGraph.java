package com.codingame.game.view;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Region;
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
		Optional<Region> region1 = gameMap.getRegionForFieldById(field1.field.id);
		Optional<Region> region2 = gameMap.getRegionForFieldById(field2.field.id);
		
		return region1.isPresent() && region2.isPresent() && region1.get().id == region2.get().id;
	}
}
