package com.codingame.game;

public enum League {
	
	LEAGUE_1(1, false), //
	LEAGUE_2(2, true), //
	LEAGUE_3(3, true); //
	
	public final int level;
	public final boolean pickCommandEnabled;
	
	private League(int level, boolean pickCommandEnabled) {
		this.level = level;
		this.pickCommandEnabled = pickCommandEnabled;
	}
	
	public static League getByLevel(int level) {
		for (League league : values()) {
			if (league.level == level) {
				return league;
			}
		}
		throw new IllegalStateException("Unknown league level: " + level);
	}
}
