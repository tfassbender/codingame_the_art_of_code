package com.codingame.game.view.map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.codingame.game.build.RandomUtil;
import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.core.GameMap;
import com.codingame.game.view.View;

public class GraphPlacementTest {
	
	private GameMap map;
	private View view;
	
	@BeforeEach
	public void setup() {
		RandomUtil.init(42);
		map = new StaticMapGenerator().createMapOneRegion();
	}
	
	@Test
	public void test() {
		view = new View(null, map);
	}
}