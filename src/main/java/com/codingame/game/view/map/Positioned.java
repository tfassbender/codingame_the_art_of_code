package com.codingame.game.view.map;

import com.codingame.game.util.Vector2D;

public interface Positioned<T> {
	
	public Vector2D pos();
	
	public void setPosition(Vector2D position);
	
	public T getValue();
}
