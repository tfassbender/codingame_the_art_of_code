package com.codingame.game.view.map;

import java.util.Set;

public interface Graph<T extends Positioned<?>> {
	
	public Set<T> getFields();
	
	public boolean isFieldsConnected(T field1, T field2);
	
	public boolean isFieldsInSameCluster(T field1, T field2);
}
