package com.codingame.game.view.map;

import java.util.ArrayList;
import java.util.List;

import com.codingame.game.util.Vector2D;

public class Cluster<T> {
	
	public Vector2D centroid;
	public List<T> entries;
	
	public Cluster() {
		entries = new ArrayList<T>();
	}
	
	public void clear() {
		entries.clear();
	}
}