import java.util.ArrayList;
import java.util.List;

import com.codingame.game.core.Field;
import com.codingame.game.core.Owner;

public class FieldWithConnection extends Field implements Comparable<FieldWithConnection> {

	List<Field> reachable = new ArrayList<Field>();

	public FieldWithConnection(int id) {
		super(id);
	}
	
	public void set(Owner owner, int troops) {
		setOwner(owner);
		setTroops(troops);
	}
	
	public void adjustTroops(int delta) {
		setTroops(getTroops() + delta);
	}
	
	public void addConnection(Field field) {
		reachable.add(field);
	}
	
	public List<Field> getConnectedFieldsWithSameOwner(boolean sameOwner) {
		List<Field> fields = new ArrayList<Field>();
		
		for (Field field : reachable) {
			if ((field.getOwner() == getOwner()) == sameOwner)
				fields.add(field);
		}
		
		return fields;
	}

	@Override
	public int compareTo(FieldWithConnection o) {
		return Integer.compare(id, o.id);
	}
}
