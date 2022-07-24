package com.codingame.game.util;

import java.util.Collection;
import java.util.Comparator;

public class DistTool<T> {
	
	Collection<Pair<T, Double>> collection;

	public DistTool(Collection<Pair<T, Double>> collection) {
		this.collection = collection;
	}
	
	public T getMinimumKey() {
		return collection.stream().sorted(new Comparator<Pair<T, Double>>() {

			@Override
			public int compare(Pair<T, Double> o1, Pair<T, Double> o2) {
				return o1.getValue().compareTo(o2.getValue());
			}

		}).map(pair -> pair.getKey()).findFirst().get();
	}
	
}
