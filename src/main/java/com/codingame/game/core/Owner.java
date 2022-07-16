package com.codingame.game.core;

/**
 * The owner of a field
 */
public enum Owner {
	
	PLAYER_1, //
	PLAYER_2, //
	NEUTRAL; //
	
	public Owner getOpponent() {
		if (this == PLAYER_1) {
			return PLAYER_2;
		}
		if (this == PLAYER_2) {
			return PLAYER_1;
		}
		return NEUTRAL;
	}
}
