package com.codingame.game.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codingame.game.core.Field;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;

public class TroopNavigator {
	
	private Map<Pair<Field, Field>, Route> connectionPositions;
	
	public TroopNavigator() {
		connectionPositions = new HashMap<Pair<Field, Field>, Route>();
	}
	
	public void addOnePartConnection(Pair<Field, Field> key, Vector2D start, Vector2D end) {
		connectionPositions.put(key, new Route(start, end));
		connectionPositions.put(key.swap(), new Route(end, start));
	}
	
	public void addTwoPartConnection(Pair<Field, Field> key, Vector2D start, Vector2D intermediate1, Vector2D intermediate2, Vector2D end) {
		connectionPositions.put(key, new Route(start, intermediate1, intermediate2, end));
		connectionPositions.put(key.swap(), new Route(end, intermediate2, intermediate1, start));
	}
	
	public Route getRoute(Pair<Field, Field> connection) {
		return connectionPositions.get(connection);
	}
}
