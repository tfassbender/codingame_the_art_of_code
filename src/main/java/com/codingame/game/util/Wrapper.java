package com.codingame.game.util;

public class Wrapper<T> {
	
	public T wrapped;
	
	public static <T> Wrapper<T> empty() {
		return of(null);
	}
	
	public static <T> Wrapper<T> of(T object) {
		Wrapper<T> wrapper = new Wrapper<>();
		wrapper.wrapped = object;
		return wrapper;
	}
	
	public boolean isEmpty() {
		return wrapped == null;
	}
	
	private Wrapper() {}
}
