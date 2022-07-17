package com.codingame.game.core;

import java.util.Optional;
import java.util.Set;

import com.codingame.game.Action;
import com.codingame.game.Action.Type;
import com.codingame.game.util.Pair;

/**
 * The game map, that contains all fields and troops.
 */
public class GameMap {
	
	public static final float TROOPS_KILLED_BY_ATTACK = 0.6f;
	public static final float TROOPS_KILLED_BY_DEFENCE = 0.7f;
	
	public final Set<Field> fields;
	public final Set<Pair<Field, Field>> connections;
	public final Set<Region> regions;
	
	/**
	 * When the killed troops are calculated the value is rounded up. 
	 * The decimal places are summed up here, so the player gains additional troops in the next deployment phase.
	 * These rounding losses are reset after the troops are deployed, so the rest of the loss is not taken to the next turn.
	 */
	private float roundingLossPlayer1; // killed by player 2 - additional deployed troops for player 1
	private float roundingLossPlayer2; // killed by player 1 - additional deployed troops for player 2
	
	public GameMap(Set<Field> fields, Set<Pair<Field, Field>> connections, Set<Region> regions) {
		this.fields = fields;
		this.connections = connections;
		this.regions = regions;
	}
	
	public void executeSimultaneously(Action action1, Action action2) {
		boolean executedSimultaneously = false;
		if (action1.getType() == Type.MOVE && action2.getType() == Type.MOVE) {
			// moves need simultaneous execution if ...
			
			// ... two fields attack each other
			if (action1.getSourceId() == action2.getTargetId() && action2.getSourceId() == action1.getTargetId()) {
				executedSimultaneously = true;
				
				// we already validated, that these fields exist (in the Referee), so calling get without isPresent won't lead to an NPE
				Field field1 = getFieldById(action1.getSourceId()).get();
				Field field2 = getFieldById(action2.getSourceId()).get();
				
				// the attacking field could have already lost troops, so the number of attackers might be limited
				int attackingTroops1 = Math.min(action2.getNumTroops(), field2.getTroops());
				int attackingTroops2 = Math.min(action1.getNumTroops(), field1.getTroops());
				
				// execute the attack simultaneously - both fields loose 60% of troops
				float killedTroopsOnField1 = Math.min(field1.getTroops(), TROOPS_KILLED_BY_ATTACK * attackingTroops1);
				float killedTroopsOnField2 = Math.min(field2.getTroops(), TROOPS_KILLED_BY_ATTACK * attackingTroops2);
				
				int killedTroopsOnField1RoundedUp = (int) Math.ceil(killedTroopsOnField1);
				int killedTroopsOnField2RoundedUp = (int) Math.ceil(killedTroopsOnField2);
				
				// update the troops on the field
				field1.setTroops(Math.max(0, field1.getTroops() - killedTroopsOnField1RoundedUp));
				field2.setTroops(Math.max(0, field2.getTroops() - killedTroopsOnField2RoundedUp));
				
				float decimalLossField1 = killedTroopsOnField1RoundedUp - killedTroopsOnField1;
				float decimalLossField2 = killedTroopsOnField2RoundedUp - killedTroopsOnField2;
				
				// update the decimal losses on both sides
				if (action1.getOwner() == Owner.PLAYER_1) {
					roundingLossPlayer1 += decimalLossField1;
					roundingLossPlayer2 += decimalLossField2;
				}
				else {
					roundingLossPlayer1 += decimalLossField2;
					roundingLossPlayer2 += decimalLossField1;
				}
				
				Field movementSource = null;
				Field movementTarget = null;
				Action winningAction = null;
				if (field1.getTroops() == 0 && field2.getTroops() > 0) {
					// move the remaining troops from field 2 to field 1
					movementSource = field2;
					movementTarget = field1;
					winningAction = action2;
				}
				else {
					// move the remaining troops from field 1 to field 2
					movementSource = field2;
					movementTarget = field1;
					winningAction = action1;
				}
				
				// move troops and change the ownership of the field
				if (movementSource != null && movementTarget != null && winningAction != null) {
					int movedTroops = Math.max(movementSource.getTroops(), winningAction.getNumTroops());
					movementSource.setTroops(movementSource.getTroops() - movedTroops);
					movementTarget.setTroops(movedTroops);
					movementTarget.setOwner(winningAction.getOwner());
				}
			}
			// ... one player attacks the source field of the other players move
			else if (action1.getTargetId() == action2.getSourceId() || action2.getTargetId() == action1.getSourceId()) {
				executedSimultaneously = true;
				
				// the actions can be executed independent, but the order is important
				if (action1.getTargetId() == action2.getSourceId()) {
					executeIndependent(action1);
					executeIndependent(action2);
				}
				else {
					executeIndependent(action2);
					executeIndependent(action1);
				}
			}
		}
		
		// the actions are not dependent
		if (!executedSimultaneously) {
			executeIndependent(action1);
			executeIndependent(action2);
		}
	}
	
	public void executeIndependent(Action action) {
		switch (action.getType()) {
			case DEPLOY:
				executeDeployment(action);
				break;
			case MOVE:
				executeMovement(action);
				break;
			case WAIT:
				// do nothing
				break;
			case PICK:
			case RANDOM:
				throw new IllegalStateException("The actions PICK and RANDOM are not handled here."); //TODO define a method for PICK and RANDOM
			default:
				throw new IllegalStateException("Unknown action type: " + action.getType());
		}
	}
	
	private void executeDeployment(Action action) {
		Field targetField = getFieldById(action.getTargetId()).get(); // we already validated that this field exists
		targetField.setTroops(targetField.getTroops() + action.getNumTroops());
	}
	
	private void executeMovement(Action action) {
		// we already validated, that these fields exist (in the Referee), so calling get without isPresent won't lead to an NPE
		Field attackingField = getFieldById(action.getSourceId()).get();
		Field defendingField = getFieldById(action.getTargetId()).get();
		
		// the attacking field could have already lost troops, so the number of attackers might be limited
		int attackingTroops = Math.min(action.getNumTroops(), attackingField.getTroops());
		// all troops on the defending field fight
		
		// 60% of attackers kill a defender - 70% of defenders kill an attacker
		float killedTroopsOnDefendingField = Math.min(defendingField.getTroops(), TROOPS_KILLED_BY_ATTACK * attackingTroops);
		float killedAttackingTroops = Math.min(attackingTroops, TROOPS_KILLED_BY_DEFENCE * defendingField.getTroops());
		
		int killedTroopsOnDefendingFieldRoundedUp = (int) Math.ceil(killedTroopsOnDefendingField);
		int killedAttackingTroopsRoundedUp = (int) Math.ceil(killedAttackingTroops);
		
		// update the troops on the field
		attackingField.setTroops(Math.max(0, attackingField.getTroops() - killedAttackingTroopsRoundedUp));
		defendingField.setTroops(Math.max(0, defendingField.getTroops() - killedTroopsOnDefendingFieldRoundedUp));
		
		float decimalLossOnAttackingField = killedAttackingTroopsRoundedUp - killedAttackingTroops;
		float decimalLossOnDefendingField = killedTroopsOnDefendingFieldRoundedUp - killedTroopsOnDefendingField;
		
		// update the decimal losses on both sides
		if (action.getOwner() == Owner.PLAYER_1) {
			roundingLossPlayer1 += decimalLossOnAttackingField;
			roundingLossPlayer2 += decimalLossOnDefendingField;
		}
		else {
			roundingLossPlayer1 += decimalLossOnDefendingField;
			roundingLossPlayer2 += decimalLossOnAttackingField;
		}
		
		if (defendingField.getTroops() == 0 && killedAttackingTroopsRoundedUp < attackingTroops) {
			// move the remaining attacking troops to the attacked field (if all defenders are killed and attackers remain)
			int movedTroops = attackingTroops - killedAttackingTroopsRoundedUp;
			attackingField.setTroops(attackingField.getTroops() - movedTroops);
			defendingField.setTroops(movedTroops);
			defendingField.setOwner(action.getOwner());
		}
	}
	
	public int calculateDeployableTroops(Owner player) {
		//TODO
		
		// TODO include roundingLossPlayer1 and roundingLossPlayer2 in calculation
		return 0;
	}
	
	/**
	 * Rounding losses are reset after the deployment, so they are not taken the the next turn.
	 */
	public void resetRoundingLosses() {
		roundingLossPlayer1 = 0;
		roundingLossPlayer2 = 0;
	}
	
	public Optional<Field> getFieldById(int id) {
		return fields.stream().filter(field -> field.id == id).findFirst();
	}
	
	public boolean isFieldsConnected(int sourceId, int targetId) {
		return connections.stream().anyMatch(pair -> pair.getKey().id == sourceId && pair.getValue().id == targetId || //
				pair.getKey().id == targetId && pair.getValue().id == sourceId);
	}
}
