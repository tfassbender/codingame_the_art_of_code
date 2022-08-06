package com.codingame.game.view.map;

import com.codingame.game.util.Vector2D;

/**
 * A value with a position of type {@link Vector2D}.
 *  
 * NOTE: The implementing type and the generic type T should implement the methods equals and hashCode.
 */
public interface Positioned<T> {
	
	public Vector2D pos();
	
	public void setPosition(Vector2D position);
}
