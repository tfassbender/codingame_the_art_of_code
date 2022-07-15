package com.codingame.game;

import com.codingame.gameengine.core.MultiplayerGameManager;

public enum League {
	
	LEAGUE_1(1), //
	LEAGUE_2(2), //
	LEAGUE_3(3); //
	
	private final int level;
	
	private League(int level) {
		this.level = level;
	}
	
	public static League getByLevel(int level) {
		for (League league : values()) {
			if (league.getLevel() == level) {
				return league;
			}
		}
		throw new IllegalStateException("Unknown league level: " + level);
	}
	
	public static League getLeague(MultiplayerGameManager<Player> gameManager) {
		return getByLevel(gameManager.getLeagueLevel());
	}
	
	public int getLevel() {
		return level;
	}
}
