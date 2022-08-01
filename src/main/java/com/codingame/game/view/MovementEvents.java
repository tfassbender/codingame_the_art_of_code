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
	
	public class MovementStep {
		int units;
		MovementType type;
		Owner owner;
		Pair<Field, Field> fightWithMovement;
		
		public MovementStep(int units, MovementType type, Owner owner) {
			this.units = units;
			this.type = type;
			this.owner = owner;
		}
		
		public void setOpponentMovement(Field f1, Field f2) {
			fightWithMovement = Pair.of(f1, f2);
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
	
	public void addFight(Field src1, Field dest1, int units1, Owner owner1,
						 Field src2, Field dest2, int units2, Owner owner2) {
		addStep(src1, dest1, units1, MovementType.Fight, owner1);
		addStep(src2, dest2, units2, MovementType.Fight, owner2);
		
		List<MovementStep> steps1 = getSteps(src1, dest1);
		steps1.get(steps1.size()-1).setOpponentMovement(src2, dest2);

		List<MovementStep> steps2 = getSteps(src2, dest2);
		steps2.get(steps2.size()-1).setOpponentMovement(src1, dest1);
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
