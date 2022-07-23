package com.codingame.game.build;

import com.codingame.game.core.GameMap;

/**
 * Generates a random map on which the game is played.
 */
public class MapGenerator {
	
	public static GameMap generateMap() {
		// until we can generate maps by our self we use random static
		return selectRandomStaticMap();
	}
	
	private static GameMap selectRandomStaticMap() {
		int selection = RandomUtil.getInstance().nextInt(3);
		StaticMapGenerator staticMaps = new StaticMapGenerator();
		
		if (selection == 0) {
			return staticMaps.createMapOneRegion();
		} else if (selection == 1) { 
			return staticMaps.createMapTwoRegions();
		} else {
			return staticMaps.createMapFiveRegions();
		}
	}
	
}
