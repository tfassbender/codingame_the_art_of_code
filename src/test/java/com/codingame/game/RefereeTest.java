package com.codingame.game;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.codingame.game.Action.Type;
import com.codingame.game.build.StaticMapGenerator;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.StartingFieldChoice;
import com.codingame.game.core.TurnType;
import com.codingame.game.util.TestUtils;

public class RefereeTest {
	
	private Referee referee;
	private GameMap map;
	
	@BeforeEach
	public void setup() throws NoSuchFieldException, IllegalAccessException {
		referee = new Referee();
		map = new StaticMapGenerator().createMapFiveRegions();
		
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
	}
}
