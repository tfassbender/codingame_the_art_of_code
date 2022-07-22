import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.Action;
import com.codingame.game.Action.Type;
import com.codingame.game.core.Field;
import com.codingame.game.core.GameMap;
import com.codingame.game.core.Owner;
import com.codingame.game.core.Region;
import com.codingame.game.core.TurnType;
import com.codingame.game.util.Pair;

public class Agent1 {
	
	private Set<Region> regions;
	private List<FieldWithConnection> fields;
	
	private boolean priorityIsLower;
	private long deployableTroops;
	private int startingFieldsLeft;
	private TurnType turnType;
	
	private List<Action> actions = new ArrayList<Action>();
	
    public static void main(String[] args) {
    	Agent1 agent = new Agent1();
        Scanner in = new Scanner(System.in);

        agent.firstRead(in);
        
        while (true) {
        	agent.read(in);
        	
        	agent.think();
        	
        	agent.print(System.out);
        }
    }
    
    public void firstRead(Scanner sc) {
    	fields = new ArrayList<FieldWithConnection>();
		regions = new HashSet<Region>();
		
		// first line: one integer - the NUMBER_OF_FIELDS in the map
		int numberOfFields = sc.nextInt();
		for (int i = 0; i < numberOfFields; i++) {
			fields.add(new FieldWithConnection(i));
		}
		
		// next line: one integer - the NUMBER_OF_CONNECTIONS between fields
		int numberOfConnections = sc.nextInt();
		for (int i = 0; i < numberOfConnections; i++) {
			// next NUMBER_OF_CONNECTIONS lines: two integers - the SOURCE_ID and the TARGET_ID of the fields that are connected (bidirectional)
			int sourceId = sc.nextInt();
			int targetId = sc.nextInt();
			
			FieldWithConnection source = fields.get(sourceId);
			FieldWithConnection target = fields.get(targetId);
		
			source.addConnection(target);
//			target.addConnection(source);
		}
		
		// next line: one integer - the NUMBER_OF_REGIONS
		int numberOfRegions = sc.nextInt();
		// next NUMBER_OF_REGIONS inputs:
		// - one line: one integer - the number of bonus troops for this region
		// - one line: one integer - the NUMBER_OF_FIELDS_PER_REGION
		// - next NUBER_OF_FIELDS_PER_REGION lines: one integer - the id of the field that belongs to the region
		for (int i = 0; i < numberOfRegions; i++) {
			Set<Field> regionFields = new HashSet<Field>();
			
			int bonusTroops = sc.nextInt();
			int numberOfFieldsInRegion = sc.nextInt();
			for (int j = 0; j < numberOfFieldsInRegion; j++) {
				int id = sc.nextInt();
				regionFields.add(fields.get(id));
			}
			
			regions.add(new Region(regionFields, bonusTroops));
		}
		
		priorityIsLower = sc.nextLine().equals("LOWER"); // UPPER OR LOWER
    }
    
    public void read(Scanner sc) {
    	Owner[] owners = {Owner.NEUTRAL, Owner.PLAYER_1, Owner.PLAYER_2};
    	
    	// TODO check why this next line is/was required
    	sc.nextLine();
		
		// first line: one string - the name of the turn type (CHOOSE_STARTING_FIELDS, DEPLOY_TROOPS or MOVE_TROOPS)
		turnType = TurnType.valueOf(sc.nextLine());
		System.err.println(turnType.name());
		
		// next line: two integers - the number of the fields that are held by each player (your input is always first)
		long playersFields = sc.nextLong();
		long otherPlayersFields = sc.nextLong();
		
		// next line: two integers - the number of troops that each player can deploy (your input is always first; 0 in all turn types but DEPLOY_TROOPS)
		deployableTroops = sc.nextLong();
		long enemyDeployableTroops = sc.nextLong();
		
		startingFieldsLeft = sc.nextInt();
		int enemyStartingFieldsLeft = sc.nextInt();
		
		// next line: the NUMBER_OF_FIELDS on the map
		long numberOfFields = sc.nextLong();
		for (int i = 0; i < numberOfFields; i++) {
			int id = sc.nextInt(); // FIELD_ID is the id of the Field
			int troops = sc.nextInt(); // NUMBER_OF_TROOPS in this field
			int owner = sc.nextInt(); // OWNER of this field (1 if the field is controlled by you; 2 if it's controlled by the opponent player; 0 if it's neutral)
			
			FieldWithConnection field = fields.get(id);
			field.set(owners[owner], troops);
		}
    }
    
    private void think() {
    	actions.clear();
    	
    	switch(turnType) {
    	case CHOOSE_STARTING_FIELDS: thinkChooseStarting(); break;
    	case DEPLOY_TROOPS: thinkDeploy(); break;
    	case MOVE_TROOPS: thinkMove(); break;
    	}
    }

    private void thinkChooseStarting() {
    	// just do random (or wait if we can't choose another)
    	actions.add(new Action(Type.RANDOM));
    }
    
    private void thinkDeploy() {
    	// place troops at random fields
    	List<FieldWithConnection> myFields = getFieldsForOwner(Owner.PLAYER_1);
    	
    	// tmp solution, while 0 fields != gameEnd
    	if (myFields.size() == 0) {
    		actions.add(new Action(Type.WAIT));
    		return;
    	}
    	
    	while(deployableTroops > 0) {
    		Field field = myFields.get((int) (Math.random()*myFields.size()));
    		
    		actions.add(new Action(Type.DEPLOY, field.id, 1));
    		
    		deployableTroops--;
    	}
    }
    
    private void thinkMove() {
    	// - attack with every single unit
    	// - do some random movements within the army
    	
    	List<FieldWithConnection> myFields = getFieldsForOwner(Owner.PLAYER_1);
    	
    	for (FieldWithConnection myField : myFields) {
    		List<Field> toAssist = myField.getConnectedFieldsWithSameOwner(true);
    		List<Field> toHarm = myField.getConnectedFieldsWithSameOwner(false);
    		List<Field> targets = toHarm.size() > 0 ? toHarm : toAssist;
    		
    		for (int i = 0; i < targets.size() && myField.getTroops() != 0; i++) {
    			// at least 1 troop
				int troops = (int) (Math.random() * myField.getTroops() - 1) +1;
				
				// use the rest
				if (i == targets.size()-1) {
					troops = myField.getTroops();
				}
				
				actions.add(new Action(Type.MOVE, myField.id, targets.get(i).id, troops));
				
				myField.adjustTroops(-troops);
			}
    	}
    }
    
    private void print(PrintStream out) {
    	// randomize order of the actions
    	Collections.shuffle(actions);
    	
		List<String> actionsAsStrings = actions.stream().map(action -> action.toString()).collect(Collectors.toList());
    	
    	out.println(String.join(";", actionsAsStrings));
    }
    
    private List<FieldWithConnection> getFieldsForOwner(Owner owner) {
    	return fields.stream().filter(field -> field.getOwner() == owner).collect(Collectors.toList());
    }
}
