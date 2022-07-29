package com.codingame.game.view.map;

import java.util.Set;

public interface Graph<T> {
	
	public Set<Positioned<T>> getFields();
	
	public boolean isFieldsConnected(Positioned<T> field1, Positioned<T> field2);
	
	public boolean isFieldsInSameCluster(Positioned<T> field1, Positioned<T> field2);
}
