package com.codingame.game.core;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.Action;
import com.codingame.game.Action.Type;
import com.codingame.game.util.Pair;

/**
 * The game map, that contains all fields and troops.
 */
public class GameMap {
	
	public static final float TROOPS_KILLED_BY_ATTACK = 0.6f;
	public static final float TROOPS_KILLED_BY_DEFENCE = 0.7f;
	
	public static final int TROOPS_PER_ROUND_DEFAULT = 5;
	public static final int TROOPS_BONUS_FIRST_DEPLOYMENT = 10;
	public static final float TROOPS_BONUS_FIELD_COUNT = 0.334f; // a bit more than 1/3 to prevent rounding issues because of the float precision
	
	public final Set<Field> fields;
	public final Set<Pair<Field, Field>> connections;
	public final Set<Region> regions;
	
	protected StartingFieldChoice startingFieldChoice;
	
	/**
	 * When the killed troops are calculated the value is rounded up. 
	 * The decimal places are summed up here, so the player gains additional troops in the next deployment phase.
	 * These rounding losses are reset after the troops are deployed, so the rest of the loss is not taken to the next turn.
	 */
	private float roundingLossPlayer1; // killed by player 2 - additional deployed troops for player 1
	private float roundingLossPlayer2; // killed by player 1 - additional deployed troops for player 2
	
	/**
	 * The number of troops that were not deployed in the previous turn.
	 */
	private int sparedDeploymentTroopsPlayer1;
	private int sparedDeploymentTroopsPlayer2;
	
	public GameMap(Set<Field> fields, Set<Pair<Field, Field>> connections, Set<Region> regions) {
		this.fields = fields;
		this.connections = connections;
		this.regions = regions;
		
		startingFieldChoice = new StartingFieldChoice(fields.size());
	}
	
	public void executeSimultaneously(Action action1, Action action2) {
		boolean executedSimultaneously = false;
		
		if (action1.getType() == Type.MOVE && action2.getType() == Type.MOVE) {
			// moves need simultaneous execution if ...
			
			if (// ... two fields attack each other
			action1.getSourceId() == action2.getTargetId() && action2.getSourceId() == action1.getTargetId() || // OR
			// ... both actions have the same target field
					action1.getTargetId() == action2.getTargetId() || // OR
					// ... one player attacks the source field of the other players move
					action1.getTargetId() == action2.getSourceId() || action2.getTargetId() == action1.getSourceId()) {
				executedSimultaneously = true;
				
				//*****************************************************************************************************
				//*** execute the fight between the moving units first
				//*****************************************************************************************************
				
				// we already validated, that these fields exist (in the Referee), so calling get without isPresent won't lead to an NPE
				Field field1 = getFieldById(action1.getSourceId()).get();
				Field field2 = getFieldById(action2.getSourceId()).get();
				
				// all troops fight, because those that would not attack would defend (all are handled as attackers with 60% kill rate)
				int attackingTroops1 = action1.getNumTroops();
				int attackingTroops2 = action2.getNumTroops();
				
				// execute the attack simultaneously - both attacking armies loose 60% of troops
				float killedTroopsInArmy1 = Math.min(attackingTroops1, TROOPS_KILLED_BY_ATTACK * attackingTroops2);
				float killedTroopsInArmy2 = Math.min(attackingTroops2, TROOPS_KILLED_BY_ATTACK * attackingTroops1);
				
				int killedTroopsInArmy1RoundedUp = (int) Math.ceil(killedTroopsInArmy1);
				int killedTroopsInArmy2RoundedUp = (int) Math.ceil(killedTroopsInArmy2);
				
				// update the troops on the field
				field1.setTroops(Math.max(0, field1.getTroops() - killedTroopsInArmy1RoundedUp));
				field2.setTroops(Math.max(0, field2.getTroops() - killedTroopsInArmy2RoundedUp));
				
				float decimalLossArmy1 = killedTroopsInArmy1RoundedUp - killedTroopsInArmy1;
				float decimalLossArmy2 = killedTroopsInArmy2RoundedUp - killedTroopsInArmy2;
				
				// update the decimal losses on both sides
				if (action1.getOwner() == Owner.PLAYER_1) {
					roundingLossPlayer1 += decimalLossArmy1;
					roundingLossPlayer2 += decimalLossArmy2;
				}
				else {
					roundingLossPlayer1 += decimalLossArmy2;
					roundingLossPlayer2 += decimalLossArmy1;
				}
				
				// calculate the number of attackers that survived the attack
				int leftAttackingTroops1 = attackingTroops1 - killedTroopsInArmy1RoundedUp;
				int leftAttackingTroops2 = attackingTroops2 - killedTroopsInArmy2RoundedUp;
				
				//*****************************************************************************************************
				//*** execute the second step if it's not a fight or if only one of the moving armies survived
				//*****************************************************************************************************
				
				// if only one army survived: let the rest of the troops of this army attack the field
				Action continuedAction = null;
				if (leftAttackingTroops1 > 0 && leftAttackingTroops2 == 0) {
					continuedAction = new Action(Type.MOVE, action1.getSourceId(), action1.getTargetId(), leftAttackingTroops1).setOwner(action1.getOwner());
				}
				else if (leftAttackingTroops2 > 0 && leftAttackingTroops1 == 0) {
					continuedAction = new Action(Type.MOVE, action2.getSourceId(), action2.getTargetId(), leftAttackingTroops2).setOwner(action2.getOwner());
				}
				// if both armies survive, but one of them just moves to a field they already own, the move is executed (because it's no second attack)
				else if (leftAttackingTroops1 > 0 && leftAttackingTroops2 > 0) {
					Action continuedAction1 = new Action(Type.MOVE, action1.getSourceId(), action1.getTargetId(), leftAttackingTroops1).setOwner(action1.getOwner());
					Action continuedAction2 = new Action(Type.MOVE, action2.getSourceId(), action2.getTargetId(), leftAttackingTroops2).setOwner(action2.getOwner());
					if (!isAttack(continuedAction1)) {
						continuedAction = continuedAction1;
					}
					else if (!isAttack(continuedAction2)) {
						continuedAction = continuedAction2;
					}
				}
				
				if (continuedAction != null) {
					executeMovement(continuedAction);
				}
			}
		}
		else if (action1.getType() == Type.PICK && action2.getType() == Type.PICK) {
			if (action1.getTargetId() == action2.getTargetId()) {
				executedSimultaneously = true;
				
				// both players chose the same starting field -> check which player has a higher priority on the field
				if (action1.getTargetId() <= startingFieldChoice.player1PriorizedMaxFieldId && action1.getOwner() == Owner.PLAYER_1 || //
						action1.getTargetId() > startingFieldChoice.player1PriorizedMaxFieldId && action1.getOwner() == Owner.PLAYER_2) {
					executeIndependent(action1);
				}
				else {
					executeIndependent(action2);
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
			case PICK:
				pickStartingField(action);
				break;
			case RANDOM:
				chooseRandomStartingField(action);
				break;
			case WAIT:
				// do nothing
				break;
			default:
				throw new IllegalStateException("Unknown action type: " + action.getType());
		}
	}
	
	private void executeDeployment(Action action) {
		Field targetField = getFieldById(action.getTargetId()).get(); // we already validated that this field exists
		targetField.setTroops(targetField.getTroops() + action.getNumTroops());
	}
	
	private void executeMovement(Action action) {
		if (isAttack(action)) {
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
		else {
			// no attack - just move the troops
			Field sourceField = getFieldById(action.getSourceId()).get();
			Field targetField = getFieldById(action.getTargetId()).get();
			
			int movedTroops = Math.min(sourceField.getTroops(), action.getNumTroops());
			
			sourceField.setTroops(sourceField.getTroops() - movedTroops);
			targetField.setTroops(targetField.getTroops() + movedTroops);
		}
	}
	
	private boolean isAttack(Action action) {
		return getFieldById(action.getSourceId()).get().getOwner() != getFieldById(action.getTargetId()).get().getOwner();
	}
	
	private void pickStartingField(Action action) {
		Field field = getFieldById(action.getTargetId()).get();
		field.setOwner(action.getOwner());
		field.setTroops(1);
		
		startingFieldChoice.decreaseStartingFieldsLeft(action.getOwner());
	}
	
	private void chooseRandomStartingField(Action action) {
		Owner owner = action.getOwner();
		List<Integer> startingFields = startingFieldChoice.getStartingFieldIdsForPlayer(owner);
		int nextStartingFieldIndex = 0;
		
		Field field;
		
		// choose the next free field from the list
		do {
			field = getFieldById(startingFields.get(nextStartingFieldIndex)).get();
			nextStartingFieldIndex++;
		} while (field.getOwner() != Owner.NEUTRAL);
		
		// deploy a starting troop on the chosen field
		field.setOwner(owner);
		field.setTroops(1);
		
		startingFieldChoice.decreaseStartingFieldsLeft(owner);
	}
	
	public int calculateDeployableTroops(Owner player, boolean firstDeployment) {
		List<Field> conqueredFields = fields.stream().filter(field -> field.getOwner() == player).collect(Collectors.toList());
		List<Region> conqueredRegions = regions.stream().filter(region -> region.isConqueredBy(player)).collect(Collectors.toList());
		
		int fieldBonusTroops = (int) (conqueredFields.size() * TROOPS_BONUS_FIELD_COUNT);
		int regionBonusTroops = conqueredRegions.stream().mapToInt(Region::getBonusTroops).sum();
		int roundingLossBonusTroops = player == Owner.PLAYER_1 ? (int) Math.floor(roundingLossPlayer1) : (int) Math.floor(roundingLossPlayer2);
		int firstDeploymentBonusTroops = firstDeployment ? TROOPS_BONUS_FIRST_DEPLOYMENT : 0;
		int sparedDeploymentBonusTroops = player == Owner.PLAYER_1 ? sparedDeploymentTroopsPlayer1 : sparedDeploymentTroopsPlayer2;
		
		return TROOPS_PER_ROUND_DEFAULT + //
				fieldBonusTroops + //
				regionBonusTroops + //
				roundingLossBonusTroops + //
				firstDeploymentBonusTroops + //
				sparedDeploymentBonusTroops;
	}
	
	public void deployNeutralTroops() {
		fields.stream().filter(field -> field.getOwner() == Owner.NEUTRAL).forEach(field -> field.setTroops(2));
	}
	
	/**
	 * Rounding losses are reset after the deployment, so they are not taken the the next turn.
	 */
	public void resetRoundingLosses() {
		roundingLossPlayer1 = 0;
		roundingLossPlayer2 = 0;
	}
	
	public void setSparedDeployingTroops(int troops, Owner player) {
		if (player == Owner.PLAYER_1) {
			sparedDeploymentTroopsPlayer1 = troops;
		}
		else if (player == Owner.PLAYER_2) {
			sparedDeploymentTroopsPlayer2 = troops;
		}
		else {
			throw new IllegalArgumentException("The parameter 'player' must be PLAYER_1 or PLAYER_2 but was " + player);
		}
	}
	
	public Optional<Field> getFieldById(int id) {
		return fields.stream().filter(field -> field.id == id).findFirst();
	}
	
	public Optional<Region> getRegionForFieldById(int fieldId) {
		return regions.stream().filter(r -> r.fields.stream().anyMatch(f -> f.id == fieldId)).findFirst();
	}
	
	public boolean isFieldsConnected(int sourceId, int targetId) {
		return connections.stream().anyMatch(pair -> pair.getKey().id == sourceId && pair.getValue().id == targetId || //
				pair.getKey().id == targetId && pair.getValue().id == sourceId);
	}
	
	public int getNumTroopsControlledByPlayer(Owner player) {
		return fields.stream().filter(field -> field.getOwner() == player).mapToInt(Field::getTroops).sum();
	}
	
	public int getNumFieldsControlledByPlayer(Owner player) {
		return (int) fields.stream().filter(field -> field.getOwner() == player).count();
	}
	
	public StartingFieldChoice getStartingFieldChoice() {
		return startingFieldChoice;
	}
}
