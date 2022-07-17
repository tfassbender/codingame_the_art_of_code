package com.codingame.game;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.codingame.game.Action.Type;
import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.core.StartingFieldChoice;
import com.codingame.game.core.TurnType;
import com.codingame.game.util.Pair;
import com.codingame.game.util.TestUtils;

public class RefereeTest {
	
	private Referee referee;
	private GameMapMock map;
	
	@BeforeEach
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		referee = new Referee();
		GameMap notMockedMap = new StaticMapGenerator().createMapFiveRegions();
		map = new GameMapMock(notMockedMap.fields, notMockedMap.connections, notMockedMap.regions);
		
		// use the same map for all tests
		TestUtils.setFieldPerReflection(referee, "map", map);
	}
	
	@Nested
	@DisplayName("Action Validation Tests")
	public class ActionValidationTests {
		
		@Test
		public void test_pick_starting_fields__valid() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_3);
			
			List<Action> actions = Arrays.asList(new Action(Type.PICK, 0), new Action(Type.PICK, 1), new Action(Type.PICK, 2));
			referee.validateActions(actions, Owner.PLAYER_1);
			
			// assert no exceptions are thrown
		}
		
		@Test
		public void test_pick_starting_fields__random() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_1);
			
			List<Action> actions = Arrays.asList(new Action(Type.RANDOM));
			referee.validateActions(actions, Owner.PLAYER_1);
			
			// assert no exceptions are thrown
		}
		
		@Test
		public void test_pick_starting_fields__invalid_action_types() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_3);
			
			List<Action> actions1 = Arrays.asList(new Action(Type.PICK, 0), new Action(Type.DEPLOY, 0, 1));
			List<Action> actions2 = Arrays.asList(new Action(Type.PICK, 0), new Action(Type.MOVE, 0, 1, 1));
			List<Action> actions3 = Arrays.asList(new Action(Type.PICK, 0), new Action(Type.WAIT));
			
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions1, Owner.PLAYER_1));
			InvalidActionException invalidAction2 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions2, Owner.PLAYER_1));
			InvalidActionException invalidAction3 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions3, Owner.PLAYER_1));
			
			assertTrue(invalidAction1.getMessage().contains("The action " + Type.DEPLOY + " cannot be used in this turn"));
			assertTrue(invalidAction2.getMessage().contains("The action " + Type.MOVE + " cannot be used in this turn"));
			assertTrue(invalidAction3.getMessage().contains("The action " + Type.WAIT + " cannot be used in 'CHOOSE_STARTING_FIELDS' turns."));
		}
		
		@Test
		public void test_pick_starting_fields__pick_command_cannot_be_used_in_league_one() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_1);
			
			List<Action> actions = Arrays.asList(new Action(Type.PICK, 0));
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction1.getMessage().contains("The command PICK is not enabled in this league."));
		}
		
		@Test
		public void test_pick_starting_fields__field_already_chosen() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_3);
			setFieldProperties(0, Owner.PLAYER_1, 0);
			
			List<Action> actions = Arrays.asList(new Action(Type.PICK, 0));
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction1.getMessage().contains("The field with the id 0 was already picked."));
		}
		
		@Test
		public void test_pick_starting_fields__field_does_not_exist() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_3);
			
			List<Action> actions = Arrays.asList(new Action(Type.PICK, 42));
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction1.getMessage().contains("A field with the id 42 does not exist."));
		}
		
		@Test
		public void test_pick_starting_fields__wrong_number_of_fields_chosen() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice(); // uses 3 starting fields
			setLeague(League.LEAGUE_3);
			
			List<Action> actions1 = Arrays.asList(new Action(Type.PICK, 0));
			List<Action> actions2 = Arrays.asList(new Action(Type.PICK, 1), new Action(Type.PICK, 2), new Action(Type.PICK, 3), new Action(Type.PICK, 4));
			
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions1, Owner.PLAYER_1));
			assertTrue(invalidAction1.getMessage().contains("Not enough fields were picked. You need to pick 3 fields but you picked only 1."));
			
			InvalidActionException invalidAction2 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions2, Owner.PLAYER_1));
			assertTrue(invalidAction2.getMessage().contains("To many fields were picked. You need to pick 3 fields but you picked 4."));
		}
		
		@Test
		public void test_pick_starting_fields__random_and_pick_actions_cannot_be_mixed() throws Exception {
			setTurnType(TurnType.CHOOSE_STARTING_FIELDS);
			mockStartingFieldChoice();
			setLeague(League.LEAGUE_3);
			
			List<Action> actions = Arrays.asList(new Action(Type.PICK, 0), new Action(Type.RANDOM));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("The actions PICK and RANDOM cannot be mixed."));
		}
		
		@Test
		public void test_deploy_troops__deploy_and_wait__valid() throws Exception {
			setTurnType(TurnType.DEPLOY_TROOPS);
			map.deployableTroops = 3;
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.DEPLOY, 0, 1), //
					new Action(Type.WAIT), // WAIT actions can be used (have no effect)
					new Action(Type.DEPLOY, 1, 1), //
					new Action(Type.DEPLOY, 0, 1)); // deploying to the same field twice is OK
			referee.validateActions(actions, Owner.PLAYER_1);
			
			// assert no exception is thrown
		}
		
		@Test
		public void test_deploy_troops__invalid_action_types() throws Exception {
			setTurnType(TurnType.DEPLOY_TROOPS);
			map.deployableTroops = 2;
			setFieldProperties(0, Owner.PLAYER_1, 1);
			
			List<Action> actions1 = Arrays.asList(new Action(Type.DEPLOY, 0, 2), new Action(Type.PICK, 0));
			List<Action> actions2 = Arrays.asList(new Action(Type.DEPLOY, 0, 2), new Action(Type.MOVE, 0, 1, 1));
			List<Action> actions3 = Arrays.asList(new Action(Type.DEPLOY, 0, 2), new Action(Type.RANDOM, 0, 1, 1));
			
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions1, Owner.PLAYER_1));
			InvalidActionException invalidAction2 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions2, Owner.PLAYER_1));
			InvalidActionException invalidAction3 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions3, Owner.PLAYER_1));
			
			assertTrue(invalidAction1.getMessage().contains("The action " + Type.PICK + " cannot be used in this turn"));
			assertTrue(invalidAction2.getMessage().contains("The action " + Type.MOVE + " cannot be used in this turn"));
			assertTrue(invalidAction3.getMessage().contains("The action " + Type.RANDOM + " cannot be used in this turn"));
		}
		
		@Test
		public void test_deploy_troops__field_not_owned() throws Exception {
			setTurnType(TurnType.DEPLOY_TROOPS);
			map.deployableTroops = 2;
			setFieldProperties(0, Owner.PLAYER_1, 1);
			
			List<Action> actions = Arrays.asList(new Action(Type.DEPLOY, 0, 1), new Action(Type.DEPLOY, 1, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot DEPLOY to field 1. You don't controll this field."));
		}
		
		@Test
		public void test_deploy_troops__field_does_not_exist() throws Exception {
			setTurnType(TurnType.DEPLOY_TROOPS);
			map.deployableTroops = 2;
			setFieldProperties(0, Owner.PLAYER_1, 1);
			
			List<Action> actions = Arrays.asList(new Action(Type.DEPLOY, 0, 1), new Action(Type.DEPLOY, 42, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("A field with the id 42 does not exist."));
		}
		
		@Test
		public void test_deploy_troops__deployed_more_troops_than_permitted() throws Exception {
			setTurnType(TurnType.DEPLOY_TROOPS);
			map.deployableTroops = 3;
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.DEPLOY, 0, 1), new Action(Type.DEPLOY, 1, 3));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot DEPLOY 4 troops in total. You can only deploy 3 troops in this turn."));
		}
		
		@Test
		public void test_deploy_troops__deployed_less_troops_than_allowed__valid() throws Exception {
			setTurnType(TurnType.DEPLOY_TROOPS);
			map.deployableTroops = 3;
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.DEPLOY, 0, 1), new Action(Type.DEPLOY, 1, 1));
			referee.validateActions(actions, Owner.PLAYER_1);
			
			// assert no exception is thrown
		}
		
		@Test
		public void test_move_troops__move_and_wait__valid() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 0, 1, 1), // movements between owned fields is allowed
					new Action(Type.WAIT), // WAIT actions can be used (have no effect)
					new Action(Type.MOVE, 1, 3, 1), // moves to a not owned field are allowed
					new Action(Type.MOVE, 1, 4, 1)); // two moves from the same starting field (to different target fields) are allowed
			referee.validateActions(actions, Owner.PLAYER_1);
			
			// assert no exception is thrown
		}
		
		@Test
		public void test_move_troops__connections_are_bidirectional() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 0, 1, 1), //
					new Action(Type.MOVE, 1, 0, 1));
			referee.validateActions(actions, Owner.PLAYER_1);
			
			// assert no exception is thrown
		}
		
		@Test
		public void test_move_troops__invalid_action_types() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			
			List<Action> actions1 = Arrays.asList(new Action(Type.MOVE, 0, 1, 1), new Action(Type.PICK, 0));
			List<Action> actions2 = Arrays.asList(new Action(Type.MOVE, 0, 1, 1), new Action(Type.DEPLOY, 0, 1));
			List<Action> actions3 = Arrays.asList(new Action(Type.MOVE, 0, 1, 1), new Action(Type.RANDOM, 0, 1, 1));
			
			InvalidActionException invalidAction1 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions1, Owner.PLAYER_1));
			InvalidActionException invalidAction2 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions2, Owner.PLAYER_1));
			InvalidActionException invalidAction3 = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions3, Owner.PLAYER_1));
			
			assertTrue(invalidAction1.getMessage().contains("The action " + Type.PICK + " cannot be used in this turn"));
			assertTrue(invalidAction2.getMessage().contains("The action " + Type.DEPLOY + " cannot be used in this turn"));
			assertTrue(invalidAction3.getMessage().contains("The action " + Type.RANDOM + " cannot be used in this turn"));
		}
		
		@Test
		public void test_move_troops__source_field_does_not_exist() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 42, 1, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE from field 42. A field with the id 42 does not exist."));
		}
		
		@Test
		public void test_move_troops__target_field_does_not_exist() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 0, 42, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE to field 42. A field with the id 42 does not exist."));
		}
		
		@Test
		public void test_move_troops__source_field_is_not_owned_by_player() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 2, 1, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE from field 2. You don't controll this field."));
		}
		
		@Test
		public void test_move_troops__source_and_target_field_are_the_same_field() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 1, 1, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE from field 1 to field 1. A move must be to a different field."));
		}
		
		@Test
		public void test_move_troops__source_and_target_field_are_not_connected() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 1, 10, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE from field 1 to field 10. The fields are not connected."));
		}
		
		@Test
		public void test_move_troops__multiple_movements_with_same_source_and_target_field() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 3);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 1, 2, 1), new Action(Type.MOVE, 1, 2, 2));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE from field 1 to field 2 with multiple commands. " + //
					"Only one move with the same source and target is allowed."));
		}
		
		@Test
		public void test_move_troops__no_troops_moved() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 1, 2, 0));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot execute a MOVE command with no troops. You must move at least 1 troop."));
		}
		
		@Test
		public void test_move_troops__more_troops_than_existing_on_the_field() throws Exception {
			setTurnType(TurnType.MOVE_TROOPS);
			setFieldProperties(0, Owner.PLAYER_1, 1);
			setFieldProperties(1, Owner.PLAYER_1, 2);
			
			List<Action> actions = Arrays.asList(new Action(Type.MOVE, 0, 1, 1), // troops that are moved to the field in this turn cannot be moved further
					new Action(Type.MOVE, 1, 2, 2), // number of troops is summed
					new Action(Type.MOVE, 1, 2, 1));
			
			InvalidActionException invalidAction = assertThrows(InvalidActionException.class, () -> referee.validateActions(actions, Owner.PLAYER_1));
			assertTrue(invalidAction.getMessage().contains("Cannot MOVE a total number of 3 (or more) troops from field 1. " + //
					"The field only contains 2 troops."));
		}
		
		private void setTurnType(TurnType type) throws NoSuchFieldException, IllegalAccessException {
			TestUtils.setFieldPerReflection(referee, "turnType", type);
		}
		
		private void mockStartingFieldChoice() throws NoSuchFieldException, IllegalAccessException {
			StartingFieldChoice startingFieldChoice = new StartingFieldChoice(0); // will set the number of starting fields to the minimum of 3
			TestUtils.setFieldPerReflection(referee, "startingFieldChoice", startingFieldChoice);
		}
	}
	
	private void setLeague(League league) throws NoSuchFieldException, IllegalAccessException {
		TestUtils.setFieldPerReflection(referee, "league", league);
	}
	
	private void setFieldProperties(int id, Owner owner, int troops) throws NoSuchFieldException, IllegalAccessException {
		TestUtils.setFieldPerReflection(map.getFieldById(id).get(), "owner", owner);
		TestUtils.setFieldPerReflection(map.getFieldById(id).get(), "troops", troops);
	}
	
	private class GameMapMock extends GameMap {
		
		public int deployableTroops;
		
		public GameMapMock(Set<Field> fields, Set<Pair<Field, Field>> connections, Set<Region> regions) {
			super(fields, connections, regions);
		}
		
		@Override
		public int calculateDeployableTroops(Owner player) {
			return deployableTroops;
		}
	}
}
