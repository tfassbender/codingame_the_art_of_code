package com.codingame.game.util;

import java.util.Collection;
import java.util.Comparator;

/*
 * Class to operate on Pair<T, Double> entries (e.g. selecting the closest T based on some distances).
 */
public class DistTool<T> {
	
	Collection<Pair<T, Double>> collection;
	
	public DistTool(Collection<Pair<T, Double>> collection) {
		this.collection = collection;
	}
	
	public T getMinimumKey() {
		return collection.stream() //
				.sorted(Comparator.comparing((Pair<?, Double> pair) -> pair.getValue())) //
				.map(pair -> pair.getKey()) //
				.findFirst().get();
	}
}
