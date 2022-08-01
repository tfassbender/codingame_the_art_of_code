package com.codingame.game.util;

import java.util.Objects;

public class Pair<K, V> {
	
	public static <K, V> Pair<K, V> of(K key, V value) {
		return new Pair<K, V>(key, value);
	}
	
	private K key;
	private V value;
	
	private Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}
	
	public K getKey() {
		return key;
	}
	
	public V getValue() {
		return value;
	}
	
	public Pair<V, K> swapped() {
		return Pair.of(value, key);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(key, value);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Pair<?, ?> other = (Pair<?, ?>) obj;
		return Objects.equals(key, other.key) && Objects.equals(value, other.value);
	}
}
