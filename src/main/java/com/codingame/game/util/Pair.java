package com.codingame.game.util;

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
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Pair)) return false;
		
		Pair other = (Pair) o;
		return key.equals(other.key) && value.equals(other.value);
	}
	
	@Override
	public int hashCode() {
		return 13*key.hashCode()+value.hashCode();
	}
	
	public String toString() {
		return String.format("Pair<%s, %s>", key.toString(), value.toString());
	}
}
