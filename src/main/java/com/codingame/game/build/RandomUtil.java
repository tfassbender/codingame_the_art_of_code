package com.codingame.game.build;

import java.util.Random;

/**
 * Provides random functions and the random seed.
 */
public class RandomUtil {
	
	private static RandomUtil instance;
	
	public static synchronized RandomUtil getInstance() {
		if (instance == null) {
			throw new IllegalStateException("RandomUtil is not initialized yet. Use the init(long) method to initialize it.");
		}
		return instance;
	}
	
	public static synchronized void init(long seed) {
		instance = new RandomUtil(seed);
	}
	
	public static synchronized void init(Random random) {
		instance = new RandomUtil(random);
	}
	
	private Random random;
	
	private RandomUtil(long seed) {
		random = new Random(seed);
	}
	
	private RandomUtil(Random random) {
		this.random = random;
	}
	
	public int nextInt(int bound) {
		return random.nextInt(bound);
	}
	
	public float nextFloat() {
		return random.nextFloat();
	}
	
	public boolean nextBoolean() {
		return random.nextBoolean();
	}
}