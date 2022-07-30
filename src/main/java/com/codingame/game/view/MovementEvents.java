package com.codingame.game.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.codingame.game.core.Field;
import com.codingame.game.core.Owner;
import com.codingame.game.util.Pair;

public class MovementEvents {

	public enum MovementType {
		Forward,
		Retreat,
		Fight,
		Die;
	}
	
	class MovementStep {
		int units;
		MovementType type;
		Owner owner;
		
		public MovementStep(int units, MovementType type, Owner owner) {
			this.units = units;
			this.type = type;
			this.owner = owner;
		}
	}
	
	Map<Pair<Field, Field>, List<MovementStep>> steps;
	
	public MovementEvents() {
		steps = new HashMap<Pair<Field, Field>, List<MovementStep>> ();
	}
	
	public void reset() {
		steps.clear();
	}
	
	public void addStep(Field src, Field dest, int units, MovementType type, Owner owner) {
		getSteps(src, dest).add(new MovementStep(units, type, owner));
	}
	
	public List<MovementStep> getSteps(Field src, Field dest) {
		Pair<Field, Field> key = Pair.of(src, dest);
		
		if (!steps.containsKey(key)) {
			steps.put(key, new ArrayList<MovementStep>());
		}
		
		return steps.get(key);
	}

	public Set<Pair<Field, Field>> getKeys() {
		return steps.keySet();
	}
}
