package com.codingame.game.core;

import java.util.HashMap;
import java.util.Map;

public class StartingFieldChoice {
	
	public static final int MIN_STARTING_FIELDS = 3;
	public static final int MAX_STARTING_FIELDS = 5;
	
	private Map<Owner, Integer> startingFieldsLeft = new HashMap<>();
	
	/*
	 * The maximum id of fields where player1's choice has a higher priority when both players choose the same starting field.
	 */
	public final int player1PriorizedMaxFieldId;
	
	public StartingFieldChoice(int numFields) {
		player1PriorizedMaxFieldId = numFields / 2 - 1; // number of fields has to be even (the map is symmetric)
		
		int numStartingFields = calculateNumStartingFields(numFields);
		startingFieldsLeft.put(Owner.PLAYER_1, numStartingFields);
		startingFieldsLeft.put(Owner.PLAYER_2, numStartingFields);
	}
	
	private int calculateNumStartingFields(int numFields) {
		int numStartingFields = (int) (0.2f * numFields); // TODO maybe find a better metric that 20%
		
		return Math.max(MIN_STARTING_FIELDS, Math.min(MAX_STARTING_FIELDS, numStartingFields));
	}
	
	public int getStartingFieldsLeft(Owner owner) {
		return startingFieldsLeft.get(owner);
	}
}
