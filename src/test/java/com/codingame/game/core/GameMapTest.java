package com.codingame.game.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.codingame.game.Action;
import com.codingame.game.Action.Type;
import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.util.TestUtils;

public class GameMapTest {
	
	private static final float EPSILON = 1e-3f;
	
	private GameMap map;
	
	@BeforeEach
	public void setup() {
		map = new StaticMapGenerator().createMapOneRegion();
	}
	
	@Nested
	@DisplayName("Move Execution Tests")
	public class MoveExecutionTests {
		
		@Test
		public void test_execute_independent__single_attack__field_conquered() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 2);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_1, defendingField.getOwner());
			assertEquals(10 - 4, attackingField.getTroops());
			assertEquals(2, defendingField.getTroops(), "2 attackers are killed, 2 move to the attacked field");
			
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.4 (2 * 0.7), so 0.6 is the rounding loss");
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6); rounding loss is 0 because the field only contained 2 troops");
		}
		
		@Test
		public void test_execute_independent__single_attack__field_conquered__killed_troops_are_rounded_up() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 2);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 2).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(10 - 2, attackingField.getTroops());
			assertEquals(0, defendingField.getTroops());
			
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.4 (2 * 0.7); rounding loss is 2 - 1.4 = 0.6");
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 1.2 (2 * 0.6); rounding loss is 2 - 1.2 = 0.8");
		}
		
		@Test
		public void test_execute_independent__single_attack__field_not_conquered() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 5);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 5).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(10 - 4, attackingField.getTroops(), "4 troops are killed; the remaining 1 moves back to the attacking field");
			assertEquals(5 - 3, defendingField.getTroops(), "3 defenders are killed");
		}
		
		@Test
		public void test_execute_independent__single_attack__field_conquered__attacking_field_is_empty_after_attack() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 5);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 10).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner(), "the attacking player keeps the controll of the attacking field (even if there are not troops left on it)");
			assertEquals(Owner.PLAYER_1, defendingField.getOwner());
			assertEquals(0, attackingField.getTroops(), "all troops are moved");
			assertEquals(10 - 4, defendingField.getTroops(), "4 troops are killed");
		}
		
		@Test
		public void test_execute_independent__single_attack__field_not_conquered__attacking_field_is_empty_after_attack() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 5);
			setFieldProperties(1, Owner.PLAYER_2, 10);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 5).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner(), "the attacking player keeps the controll of the attacking field (even if there are not troops left on it)");
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(0, attackingField.getTroops(), "all attacker troops are killed");
			assertEquals(10 - 3, defendingField.getTroops(), "3 defender troops are killed");
		}
		
		@Test
		public void test_execute_independent__single_attack__empty_field_attacked() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 5);
			setFieldProperties(1, Owner.PLAYER_2, 0);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 1).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_1, defendingField.getOwner());
			assertEquals(5 - 1, attackingField.getTroops());
			assertEquals(1, defendingField.getTroops());
		}
		
		@Test
		public void test_execute_independent__single_attack__all_troops_killed_on_both_sides() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_2, 1);
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 1).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(0, attackingField.getTroops());
			assertEquals(0, defendingField.getTroops());
		}
		
		@Test
		public void test_execute_independent__two_attacks__rounding_losses_are_summed() throws Exception {
			setFieldProperties(1, Owner.PLAYER_1, 8);
			setFieldProperties(2, Owner.PLAYER_2, 3);
			setFieldProperties(3, Owner.PLAYER_2, 3);
			
			map.executeIndependent(new Action(Type.MOVE, 1, 2, 4).setOwner(Owner.PLAYER_1));
			map.executeIndependent(new Action(Type.MOVE, 1, 3, 4).setOwner(Owner.PLAYER_1));
			
			assertEquals(1.8f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 2.1 (3 * 0.7) in two battels; rounding loss is 1.8 (2 * 0.9)");
			assertEquals(1.2f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6) in two battles; rounding loss is 1.2 (2 * 0.6)");
		}
		
		@Test
		public void test_execute_independent__single_attack__more_troops_attacking_that_present_on_the_attacker_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 3);
			setFieldProperties(1, Owner.PLAYER_2, 3);
			
			// the action is allowed, because there could have been 5 troops on the attacking field, but 2 were killed in an earlier attack (towards the attacking field)
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 5).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(0, attackingField.getTroops());
			assertEquals(1, defendingField.getTroops(), "only 2 defenders were killed, because the attacking field contained only 3 attackers");
		}
		
		@Test
		public void test_execute_independent__move_without_attack() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 3);
			setFieldProperties(1, Owner.PLAYER_1, 3); // both fields are controlled by PLAYER_1
			
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 2).setOwner(Owner.PLAYER_1));
			
			Field sourceField = map.getFieldById(0).get();
			Field targetField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, sourceField.getOwner());
			assertEquals(Owner.PLAYER_2, targetField.getOwner());
			assertEquals(1, sourceField.getTroops());
			assertEquals(5, targetField.getTroops(), "troops are summed, because both fields are controlled by PLAYER_1");
			
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "no battles were executed, so there is no rounding loss");
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "no battles were executed, so there is no rounding loss");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 10);
			
			map.executeSimultaneously(new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1), new Action(Type.MOVE, 1, 0, 7).setOwner(Owner.PLAYER_2));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(10 - 5, field0.getTroops(), "5 troops are killed (Math.ceil(7 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			assertEquals(10 - 3, field1.getTroops(), "3 troops are killed (Math.ceil(4 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 2.4 (4 * 0.6); rounding loss is 0.6");
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 4.2 (7 * 0.6); rounding loss is 0.8");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__field_1_is_conquered() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 2);
			
			map.executeSimultaneously(new Action(Type.MOVE, 0, 1, 5).setOwner(Owner.PLAYER_1), new Action(Type.MOVE, 1, 0, 1).setOwner(Owner.PLAYER_2));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_1, field1.getOwner());
			assertEquals(4, field0.getTroops(), "5 troops move to field 1 and 1 troop is killed on field 0");
			assertEquals(5, field1.getTroops(), "all defenders are killed; 5 troops move to field 1");
			
			assertEquals(0.4f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 0.6 (1 * 0.6); rounding loss is 0.4");
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.0 (5 * 0.6); rounding loss is 0.0");
		}
		
		@Test
		public void test_execute_simultaneously__move_out_of_attacked_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 5);
			setFieldProperties(2, Owner.PLAYER_2, 0);
			
			// the attack (from field 0 to field 1) is to be executed first
			map.executeSimultaneously(new Action(Type.MOVE, 1, 2, 5).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 6).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 4, attackingField.getTroops(), "4 troops are killed; 2 troops move back to field 0, because not all defenders were killed");
			assertEquals(0, defendingField.getTroops(), "4 defenders are killed; the remaining 1 defender moves to field 2");
			assertEquals(1, field2.getTroops(), "4 defenders are killed; the remaining 1 defender moves to field 2");
			
			assertEquals(0.5f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 3.5 (5 * 0.7); rounding loss is 0.5");
			assertEquals(0.4f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.6 (6 * 0.6); rounding loss is 0.4");
		}
		
		@Test
		public void test_execute_simultaneously__move_into_attacked_field__one_of_the_players_owns_the_attacked_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 1);
			setFieldProperties(2, Owner.PLAYER_2, 5);
			
			// the player that owns the field moves first
			map.executeSimultaneously(new Action(Type.MOVE, 2, 1, 5).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 6).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 5, attackingField.getTroops(), "5 troops are killed; 1 troop moves back to field 0, because not all defenders were killed");
			assertEquals(1, defendingField.getTroops(), "4 defenders are killed");
			assertEquals(0, field2.getTroops(), "all troops are moved to the defending field");
			
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 4.2 (6 * 0.7); rounding loss is 0.8");
			assertEquals(0.4f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.6 (6 * 0.6); rounding loss is 0.4");
		}
		
		@Test
		public void test_execute_simultaneously__move_into_attacked_field__the_attacked_field_is_neutral__player_1_conqueres_the_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.NEUTRAL, 1);
			setFieldProperties(2, Owner.PLAYER_2, 5);
			
			// both players are attacking (handled like the fields are attacking each other); neutral troops are ignored
			map.executeSimultaneously(new Action(Type.MOVE, 2, 1, 2).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 6).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field attackedField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_1, attackedField.getOwner());
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 6 - 2, field0.getTroops(), "2 troops are killed; 5 troop move to the attacked field");
			assertEquals(5, attackedField.getTroops(), "5 troops move from field 0 to the attacked field");
			assertEquals(5 - 2, field2.getTroops(), "both attacking troops are killed");
			
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.2 (2 * 0.6); rounding loss is 0.8");
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.6 (6 * 0.6); rounding loss is 0.0, because only 2 troops can be killed");
		}
		
		@Test
		public void test_execute_simultaneously__move_into_attacked_field__the_attacked_field_is_neutral__noone_conqueres_the_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.NEUTRAL, 1);
			setFieldProperties(2, Owner.PLAYER_2, 5);
			
			// both players are attacking (handled like the fields are attacking each other); neutral troops are ignored
			map.executeSimultaneously(new Action(Type.MOVE, 2, 1, 4).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field attackedField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.NEUTRAL, attackedField.getOwner(), "the field was not conquered by a player");
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 3, field0.getTroops(), "3 troops are killed; 2 troop move back to field 0 because not all enemies were killed");
			assertEquals(0, attackedField.getTroops(), "all neutral troops are killed (defined in this case; no matter how many troops attack)");
			assertEquals(5 - 3, field2.getTroops(), "3 troops are killed; 2 troops move back to field 2 because not all enemies were killed");
			
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 2.4 (4 * 0.6); rounding loss is 0.6");
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6); rounding loss is 0.6");
		}
	}
	
	@Nested
	@DisplayName("Deployment Tests")
	public class DeploymentTests {
		
		@Test
		public void test_deploy_simultaneously() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 4);
			setFieldProperties(1, Owner.PLAYER_2, 2);
			
			map.executeSimultaneously(new Action(Type.DEPLOY, 0, 2), new Action(Type.DEPLOY, 1, 5)); // the deployments are handled independent
			
			assertEquals(4 + 2, map.getFieldById(0).get().getTroops());
			assertEquals(2 + 5, map.getFieldById(1).get().getTroops());
		}
		
		@Test
		public void test_deploy_independent() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 4);
			setFieldProperties(1, Owner.PLAYER_2, 2);
			
			map.executeIndependent(new Action(Type.DEPLOY, 0, 2));
			map.executeIndependent(new Action(Type.DEPLOY, 1, 5));
			
			assertEquals(4 + 2, map.getFieldById(0).get().getTroops());
			assertEquals(2 + 5, map.getFieldById(1).get().getTroops());
		}
	}
	
	private void setFieldProperties(int id, Owner owner, int troops) throws NoSuchFieldException, IllegalAccessException {
		TestUtils.setFieldPerReflection(map.getFieldById(id).get(), "owner", owner);
		TestUtils.setFieldPerReflection(map.getFieldById(id).get(), "troops", troops);
	}
	
	private float getRoundingLoss(Owner owner) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
		if (owner == Owner.PLAYER_1) {
			return TestUtils.getFieldPerReflection(map, "roundingLossPlayer1");
		}
		else if (owner == Owner.PLAYER_2) {
			return TestUtils.getFieldPerReflection(map, "roundingLossPlayer2");
		}
		else {
			throw new IllegalArgumentException("Use PLAYER_1 or PLAYER_2 as argument! Not " + owner);
		}
	}
}
