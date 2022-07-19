package com.codingame.game.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.codingame.game.build.RandomUtil;
import com.codingame.game.util.Pair;

public class StartingFieldChoice {
	
	public static final int MIN_STARTING_FIELDS = 2;
	public static final int MAX_STARTING_FIELDS = 5;
	
	private Map<Owner, Integer> startingFieldsLeft = new HashMap<>();
	
	private List<Pair<Integer, Integer>> randomStartingFields;
	
	/*
	 * The maximum id of fields where player1's choice has a higher priority when both players choose the same starting field.
	 */
	public final int player1PriorizedMaxFieldId;
	
	private int numFields;
	private int numStartingFields;
	
	public StartingFieldChoice(int numFields) {
		this.numFields = numFields;
		player1PriorizedMaxFieldId = numFields / 2 - 1; // number of fields has to be even (the map is symmetric)
		
		numStartingFields = calculateNumStartingFields(numFields);
		startingFieldsLeft.put(Owner.PLAYER_1, numStartingFields);
		startingFieldsLeft.put(Owner.PLAYER_2, numStartingFields);
		
		randomStartingFields = chooseRandomStartingFields();
	}
	
	private int calculateNumStartingFields(int numFields) {
		int numStartingFields = (int) (0.2f * numFields); // TODO maybe find a better metric that 20%
		
		return Math.max(MIN_STARTING_FIELDS, Math.min(MAX_STARTING_FIELDS, numStartingFields));
	}
	
	private List<Pair<Integer, Integer>> chooseRandomStartingFields() {
		List<Pair<Integer, Integer>> randomStartingFields = new ArrayList<>();
		final int halfFields = numFields / 2;
		RandomUtil random = RandomUtil.getInstance();
		
		boolean[] fieldChosen = new boolean[halfFields];
		
		/*
		 * Choose twice the number of starting fields, so there are enough starting fields even if all 
		 * but one fields are already chosen and are the same as the random fields.
		 */
		for (int i = 0; i < 2 * numStartingFields; i++) {
			// the first field id has a maximum of half the number of fields, so the fields are chosen symmetrically
			int firstFieldId = random.nextInt(halfFields);
			
			// prevent choosing a field twice
			while (fieldChosen[firstFieldId]) {
				firstFieldId = (firstFieldId + 1) % halfFields;
			}
			fieldChosen[firstFieldId] = true;
			
			int secondFieldId = firstFieldId + halfFields;
			boolean swap = random.nextBoolean(); // randomly swap first and second field, so the starting fields can be on both sides of the field
			
			if (swap) {
				randomStartingFields.add(Pair.of(secondFieldId, firstFieldId));
			}
			else {
				randomStartingFields.add(Pair.of(firstFieldId, secondFieldId));
			}
		}
		
		return randomStartingFields;
	}
	
	public int getStartingFieldsLeft(Owner owner) {
		return startingFieldsLeft.get(owner);
	}
	
	protected void decreaseStartingFieldsLeft(Owner owner) {
		startingFieldsLeft.put(owner, startingFieldsLeft.get(owner) - 1);
	}
	
	public List<Integer> getStartingFieldIdsForPlayer(Owner owner) {
		if (owner == Owner.PLAYER_1) {
			return randomStartingFields.stream().map(Pair::getKey).collect(Collectors.toList());
		}
		else if (owner == Owner.PLAYER_2) {
			return randomStartingFields.stream().map(Pair::getValue).collect(Collectors.toList());
		}
		else {
			throw new IllegalArgumentException("The owner must be PLAYER_1 or PLAYER_2, but was " + owner);
		}
	}
}
