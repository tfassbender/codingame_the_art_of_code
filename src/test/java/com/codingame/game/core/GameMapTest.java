package com.codingame.game.core;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import com.codingame.game.Action;
import com.codingame.game.Action.Type;
import com.codingame.game.build.RandomUtil;
import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.util.TestUtils;

public class GameMapTest {
	
	private static final float EPSILON = 1e-3f;
	
	private GameMap map;
	
	@BeforeAll
	public static void initRandomUtil() throws NoSuchFieldException, IllegalAccessException {
		RandomUtil.init(42);
	}
	
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
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner(), "the attacking player keeps the control of the attacking field (even if there are not troops left on it)");
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
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner(), "the attacking player keeps the control of the attacking field (even if there are not troops left on it)");
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
			assertEquals(Owner.PLAYER_1, targetField.getOwner());
			assertEquals(1, sourceField.getTroops());
			assertEquals(5, targetField.getTroops(), "troops are summed, because both fields are controlled by PLAYER_1");
			
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "no battles were executed, so there is no rounding loss");
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "no battles were executed, so there is no rounding loss");
		}
		
		@Test
		public void test_execute_independent__move_without_attack__more_troops_moved_than_present_on_the_source_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 3);
			setFieldProperties(1, Owner.PLAYER_1, 3); // both fields are controlled by PLAYER_1
			
			// the action is allowed, because there could have been 5 troops on the attacking field, but 2 were killed in an earlier attack (towards the attacking field)
			map.executeIndependent(new Action(Type.MOVE, 0, 1, 5).setOwner(Owner.PLAYER_1));
			
			Field sourceField = map.getFieldById(0).get();
			Field targetField = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, sourceField.getOwner());
			assertEquals(Owner.PLAYER_1, targetField.getOwner());
			assertEquals(0, sourceField.getTroops());
			assertEquals(6, targetField.getTroops(), "troops are summed, because both fields are controlled by PLAYER_1");
			
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "no battles were executed, so there is no rounding loss");
			assertEquals(0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "no battles were executed, so there is no rounding loss");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered__one_attacking_army_survives() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 9);
			setFieldProperties(1, Owner.PLAYER_2, 8);
			
			map.executeSimultaneously(new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1), new Action(Type.MOVE, 1, 0, 7).setOwner(Owner.PLAYER_2));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(9 - 4 - 3, field0.getTroops(), "all 4 attackers are killed in the first attack (7 * 0.6); 3 troops are killed in the second attack (4 * 0.6)");
			assertEquals(8 - 3 - 4, field1.getTroops(), "3 troops are killed in the first attack (4 * 0.6); all 4 attacking troops are killed in the second attack (5 * 0.7)");

			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are more than present in the first fight and 2.4 (4 * 0.6) in the second fight; rounding loss is 0.6");
			assertEquals(1.1f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6) in the first fight and 3.5 (5 * 0.7) in the second fight; rounding loss is 1.1 (0.6 + 0.5)");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered__one_attacking_army_survives__swapped_action_positions() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 9);
			setFieldProperties(1, Owner.PLAYER_2, 8);
			
			// just like the test above, but the actions are swapped
			map.executeSimultaneously(new Action(Type.MOVE, 1, 0, 7).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(9 - 4 - 3, field0.getTroops(), "all 4 attackers are killed in the first attack (7 * 0.6); 3 troops are killed in the second attack (4 * 0.6)");
			assertEquals(8 - 3 - 4, field1.getTroops(), "3 troops are killed in the first attack (4 * 0.6); all 4 attacking troops are killed in the second attack (5 * 0.7)");
			
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are more than present in the first fight and 2.4 (4 * 0.6) in the second fight; rounding loss is 0.6");
			assertEquals(1.1f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6) in the first fight and 3.5 (5 * 0.7) in the second fight; rounding loss is 1.1 (0.6 + 0.5)");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered__both_attacking_armies_survive() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 9);
			setFieldProperties(1, Owner.PLAYER_2, 8);
			
			map.executeSimultaneously(new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1), new Action(Type.MOVE, 1, 0, 5).setOwner(Owner.PLAYER_2));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(9 - 3, field0.getTroops(), "3 troops are killed (Math.ceil(5 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			assertEquals(8 - 3, field1.getTroops(), "3 troops are killed (Math.ceil(4 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 3.0 (5 * 0.6); rounding loss is 0.0");
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6); rounding loss is 0.6");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered__both_attacking_armies_survive__swapped_action_positions() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 9);
			setFieldProperties(1, Owner.PLAYER_2, 8);
			
			// just like the test above, but the actions are swapped
			map.executeSimultaneously(new Action(Type.MOVE, 1, 0, 5).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 4).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(9 - 3, field0.getTroops(), "3 troops are killed (Math.ceil(5 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			assertEquals(8 - 3, field1.getTroops(), "3 troops are killed (Math.ceil(4 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 3.0 (5 * 0.6); rounding loss is 0.0");
			assertEquals(0.6f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 2.4 (4 * 0.6); rounding loss is 0.6");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered__no_attacking_armies_survive() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 9);
			setFieldProperties(1, Owner.PLAYER_2, 8);
			
			map.executeSimultaneously(new Action(Type.MOVE, 0, 1, 2).setOwner(Owner.PLAYER_1), new Action(Type.MOVE, 1, 0, 2).setOwner(Owner.PLAYER_2));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(9 - 2, field0.getTroops(), "2 troops are killed (Math.ceil(2 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			assertEquals(8 - 2, field1.getTroops(), "2 troops are killed (Math.ceil(2 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.2 (2 * 0.6); rounding loss is 0.8");
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 1.2 (2 * 0.6); rounding loss is 0.8");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__no_field_is_conquered__no_attacking_armies_survivee__swapped_action_positions() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 9);
			setFieldProperties(1, Owner.PLAYER_2, 8);
			
			// just like the test above, but the actions are swapped
			map.executeSimultaneously(new Action(Type.MOVE, 1, 0, 2).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 2).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_2, field1.getOwner());
			assertEquals(9 - 2, field0.getTroops(), "2 troops are killed (Math.ceil(2 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			assertEquals(8 - 2, field1.getTroops(), "2 troops are killed (Math.ceil(2 * 0.6); both kill factors are 0.6 this time, because both are attackers)");
			
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.2 (2 * 0.6); rounding loss is 0.8");
			assertEquals(0.8f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 1.2 (2 * 0.6); rounding loss is 0.8");
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
			assertEquals(5, field0.getTroops(), "5 troops attack field 1 and 2 of the troops are killed");
			assertEquals(3, field1.getTroops(), "all defenders are killed; 3 attacking troops survive and move to field 1");
			
			assertEquals(0.7f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.3 (1 * 0.6 + 1 * 0.7); rounding loss is 0.7");
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are more than present on the opponent armies; rounding loss is 0.0");
		}
		
		@Test
		public void test_execute_simultaneously__fields_attacking_each_other__field_1_is_conquered__swapped_action_positions() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 2);
			
			// just like the test above, but the actions are swapped
			map.executeSimultaneously(new Action(Type.MOVE, 1, 0, 1).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 5).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field field1 = map.getFieldById(1).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_1, field1.getOwner());
			assertEquals(5, field0.getTroops(), "5 troops attack field 1 and 2 of the troops are killed");
			assertEquals(3, field1.getTroops(), "all defenders are killed; 3 attacking troops survive and move to field 1");
			
			assertEquals(0.7f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.3 (1 * 0.6 + 1 * 0.7); rounding loss is 0.7");
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are more than present on the opponent armies; rounding loss is 0.0");
		}
		
		@Test
		public void test_execute_simultaneously__move_out_of_attacked_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 5);
			setFieldProperties(2, Owner.PLAYER_2, 0);
			
			// the moving troops fight first (5 against 6)
			map.executeSimultaneously(new Action(Type.MOVE, 1, 2, 5).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 6).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 3, attackingField.getTroops(), "3 troops are killed; 2 troops move back to field 0, because not all opponent attackers were killed");
			assertEquals(0, defendingField.getTroops(), "4 moving troops are killed; the remaining 1 troop moves to field 2");
			assertEquals(1, field2.getTroops(), "4 moving troops are killed; the remaining 1 troop moves to field 2");
			
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 3.0 (5 * 0.6); rounding loss is 0.0");
			assertEquals(0.4f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.6 (6 * 0.6); rounding loss is 0.4");
		}
		
		@Test
		public void test_execute_simultaneously__move_into_attacked_field__one_of_the_players_owns_the_attacked_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.PLAYER_2, 1);
			setFieldProperties(2, Owner.PLAYER_2, 5);
			
			// the moving troops fight first (5 against 6)
			map.executeSimultaneously(new Action(Type.MOVE, 2, 1, 5).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 6).setOwner(Owner.PLAYER_1));
			
			Field attackingField = map.getFieldById(0).get();
			Field defendingField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, attackingField.getOwner());
			assertEquals(Owner.PLAYER_2, defendingField.getOwner());
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 3, attackingField.getTroops(), "3 troops are killed; 3 troop moves back to field 0, because not all defenders were killed");
			assertEquals(2, defendingField.getTroops(), "4 moving troops are killed");
			assertEquals(0, field2.getTroops(), "all remaining troops are moved to the defending field");
			
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 3.0 (5 * 0.6); rounding loss is 0.0");
			assertEquals(0.4f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.6 (6 * 0.6); rounding loss is 0.4");
		}
		
		@Test
		public void test_execute_simultaneously__move_into_attacked_field__the_attacked_field_is_neutral__player_1_conqueres_the_field() throws Exception {
			setFieldProperties(0, Owner.PLAYER_1, 10);
			setFieldProperties(1, Owner.NEUTRAL, 1);
			setFieldProperties(2, Owner.PLAYER_2, 5);
			
			// the moving troops fight first (2 against 6)
			map.executeSimultaneously(new Action(Type.MOVE, 2, 1, 2).setOwner(Owner.PLAYER_2), new Action(Type.MOVE, 0, 1, 6).setOwner(Owner.PLAYER_1));
			
			Field field0 = map.getFieldById(0).get();
			Field attackedField = map.getFieldById(1).get();
			Field field2 = map.getFieldById(2).get();
			
			assertEquals(Owner.PLAYER_1, field0.getOwner());
			assertEquals(Owner.PLAYER_1, attackedField.getOwner());
			assertEquals(Owner.PLAYER_2, field2.getOwner());
			assertEquals(10 - 6, field0.getTroops(), "2 troops are killed; the other 4 troops move to the attacked field");
			assertEquals(3, attackedField.getTroops(), "4 troops move on to the attacked field (second fight); 1 of them is killed, 3 move to the attacked field");
			assertEquals(5 - 2, field2.getTroops(), "2 attacking troops are killed");
			
			assertEquals(1.1f, getRoundingLoss(Owner.PLAYER_1), EPSILON, "kills are 1.2 (2 * 0.6) + 0.7 (1 * 0.7); rounding loss is 1.1 (0.8 + 0.3)");
			assertEquals(0.0f, getRoundingLoss(Owner.PLAYER_2), EPSILON, "kills are 3.6 (6 * 0.6); rounding loss is 0.0, because only 2 troops can be killed");
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
	
	@Nested
	@DisplayName("Random Starting Position Tests")
	public class RandomStartingPositionTests {
		
		@RepeatedTest(5)
		public void test_choose_random_starting_fields__symmetric_field() {
			RandomUtil.init(new Random().nextLong());
			map = new StaticMapGenerator().createMapOneRegion();
			
			int fieldsToChoose = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1);
			
			for (int i = 0; i < fieldsToChoose; i++) {
				map.executeSimultaneously(new Action(Type.RANDOM).setOwner(Owner.PLAYER_1), new Action(Type.RANDOM).setOwner(Owner.PLAYER_2));
			}
			
			List<Field> fieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).collect(Collectors.toList());
			List<Field> fieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).collect(Collectors.toList());
			
			assertEquals(fieldsPlayer1.size(), fieldsPlayer2.size());
			
			int halfMapSize = map.fields.size() / 2;
			for (int i = 0; i < fieldsPlayer1.size(); i++) {
				int id1 = fieldsPlayer1.get(i).id;
				int id2;
				if (id1 < halfMapSize) {
					id2 = id1 + halfMapSize;
				}
				else {
					id2 = id1 - halfMapSize;
				}
				
				assertEquals(Owner.PLAYER_2, map.getFieldById(id2).get().getOwner(), "The fields should to be symmetric");
			}
		}
		
		@Test
		public void test_choose_random_starting_fields_simultaneously() {
			int startingFieldsToChoose = map.startingFieldChoice.getStartingFieldsLeft(Owner.PLAYER_1);
			
			map.executeSimultaneously(new Action(Type.RANDOM).setOwner(Owner.PLAYER_1), new Action(Type.RANDOM).setOwner(Owner.PLAYER_2));
			
			long numFieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).count();
			long numFieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).count();
			
			assertEquals(1, numFieldsPlayer1);
			assertEquals(1, numFieldsPlayer2);
			assertEquals(startingFieldsToChoose - 1, map.startingFieldChoice.getStartingFieldsLeft(Owner.PLAYER_1));
			assertEquals(startingFieldsToChoose - 1, map.startingFieldChoice.getStartingFieldsLeft(Owner.PLAYER_2));
		}
		
		@Test
		public void test_choose_random_starting_field_simultaneously__player_1_already_chose_a_field() {
			map.getFieldById(0).get().setOwner(Owner.PLAYER_1);
			map.startingFieldChoice.decreaseStartingFieldsLeft(Owner.PLAYER_1);
			
			map.executeSimultaneously(new Action(Type.RANDOM).setOwner(Owner.PLAYER_1), new Action(Type.RANDOM).setOwner(Owner.PLAYER_2));
			
			long numFieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).count();
			long numFieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).count();
			
			assertEquals(2, numFieldsPlayer1);
			assertEquals(1, numFieldsPlayer2);
			assertEquals(map.startingFieldChoice.getStartingFieldsLeft(Owner.PLAYER_1), map.startingFieldChoice.getStartingFieldsLeft(Owner.PLAYER_2) - 1, //
					"player 2 should still have 1 field more to choose than player 1");
		}
	}
	
	@Nested
	@DisplayName("Pick Starting Position Tests")
	public class PickStartingPositionTests {
		
		@Test
		public void test_pick_starting_field__independent() {
			int startingFieldsToChoose = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1);
			
			map.executeSimultaneously(new Action(Type.PICK, 0).setOwner(Owner.PLAYER_1), new Action(Type.PICK, 1).setOwner(Owner.PLAYER_2));
			
			List<Field> fieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).collect(Collectors.toList());
			List<Field> fieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).collect(Collectors.toList());
			
			assertEquals(1, fieldsPlayer1.size());
			assertEquals(1, fieldsPlayer2.size());
			
			assertEquals(0, fieldsPlayer1.stream().findFirst().get().id);
			assertEquals(1, fieldsPlayer2.stream().findFirst().get().id);
			
			assertEquals(startingFieldsToChoose - 1, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1));
			assertEquals(startingFieldsToChoose - 1, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2));
		}
		
		@Test
		public void test_pick_starting_field__player_1_priorized_field() {
			int startingFieldsToChoose = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1);
			
			map.executeSimultaneously(new Action(Type.PICK, 0).setOwner(Owner.PLAYER_1), new Action(Type.PICK, 0).setOwner(Owner.PLAYER_2));
			
			List<Field> fieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).collect(Collectors.toList());
			List<Field> fieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).collect(Collectors.toList());
			
			assertEquals(1, fieldsPlayer1.size());
			assertEquals(0, fieldsPlayer2.size());
			
			assertEquals(0, fieldsPlayer1.stream().findFirst().get().id);
			
			assertEquals(startingFieldsToChoose - 1, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1));
			assertEquals(startingFieldsToChoose, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2));
		}
		
		@Test
		public void test_pick_starting_field__player_1_priorized_field__swapped_actions() {
			int startingFieldsToChoose = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1);
			
			// just like the test above, but the actions are swapped
			map.executeSimultaneously(new Action(Type.PICK, 0).setOwner(Owner.PLAYER_2), new Action(Type.PICK, 0).setOwner(Owner.PLAYER_1));
			
			List<Field> fieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).collect(Collectors.toList());
			List<Field> fieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).collect(Collectors.toList());
			
			assertEquals(1, fieldsPlayer1.size());
			assertEquals(0, fieldsPlayer2.size());
			
			assertEquals(0, fieldsPlayer1.stream().findFirst().get().id);
			
			assertEquals(startingFieldsToChoose - 1, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1));
			assertEquals(startingFieldsToChoose, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2));
		}
		
		@Test
		public void test_pick_starting_field__player_2_priorized_field() {
			int startingFieldsToChoose = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1);
			
			map.executeSimultaneously(new Action(Type.PICK, 5).setOwner(Owner.PLAYER_1), new Action(Type.PICK, 5).setOwner(Owner.PLAYER_2));
			
			List<Field> fieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).collect(Collectors.toList());
			List<Field> fieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).collect(Collectors.toList());
			
			assertEquals(0, fieldsPlayer1.size());
			assertEquals(1, fieldsPlayer2.size());
			
			assertEquals(5, fieldsPlayer2.stream().findFirst().get().id);
			
			assertEquals(startingFieldsToChoose, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1));
			assertEquals(startingFieldsToChoose - 1, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2));
		}
		
		@Test
		public void test_pick_starting_field__player_2_priorized_field__swapped_actions() {
			int startingFieldsToChoose = map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1);
			
			// just like the test above, but the actions are swapped
			map.executeSimultaneously(new Action(Type.PICK, 5).setOwner(Owner.PLAYER_2), new Action(Type.PICK, 5).setOwner(Owner.PLAYER_1));
			
			List<Field> fieldsPlayer1 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_1).collect(Collectors.toList());
			List<Field> fieldsPlayer2 = map.fields.stream().filter(field -> field.getOwner() == Owner.PLAYER_2).collect(Collectors.toList());
			
			assertEquals(0, fieldsPlayer1.size());
			assertEquals(1, fieldsPlayer2.size());
			
			assertEquals(5, fieldsPlayer2.stream().findFirst().get().id);
			
			assertEquals(startingFieldsToChoose, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_1));
			assertEquals(startingFieldsToChoose - 1, map.getStartingFieldChoice().getStartingFieldsLeft(Owner.PLAYER_2));
		}
	}
	
	@Nested
	@DisplayName("Deployable Troops Tests")
	public class DeployableTroopsTests {
		
		@Test
		public void test_calculate_deployable_troops() throws Exception {
			map = new StaticMapGenerator().createMapFiveRegions();
			
			Region conqueredRegion = map.regions.stream().filter(region -> region.bonusTroops == 5).findFirst().get();
			conqueredRegion.fields.forEach(field -> field.setOwner(Owner.PLAYER_1)); // 5 fields with a bonus of 5 troops for the region (+1 troops for the number of fields)
			map.setSparedDeployingTroops(3, Owner.PLAYER_1); // adds 3 troops that were not deployed in the last turn
			TestUtils.setFieldPerReflection(map, "roundingLossPlayer1", 2.9f); // rounded down to 2
			
			int deployableTroops1 = map.calculateDeployableTroops(Owner.PLAYER_1, true);
			int deployableTroops2 = map.calculateDeployableTroops(Owner.PLAYER_2, true);
			
			assertEquals(5 + 1 + 3 + 2 + GameMap.TROOPS_BONUS_FIRST_DEPLOYMENT + GameMap.TROOPS_PER_ROUND_DEFAULT, deployableTroops1);
			assertEquals(GameMap.TROOPS_BONUS_FIRST_DEPLOYMENT + GameMap.TROOPS_PER_ROUND_DEFAULT, deployableTroops2);
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
