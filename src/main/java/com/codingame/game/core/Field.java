package com.codingame.game.core;

/**
 * A field in the map, that contains troops and can be conquered.
 */
public class Field {
	
	public final int id;
	private int troops;
	private Owner owner;
	
	public Field(int id) {
		this.id = id;
	}
	
	public static Field createNeutralCamp(int id, int troops) {
		Field field = new Field(id);
		
		field.setOwner(Owner.NEUTRAL);
		field.setTroops(troops);
		
		return field;
	}
	
	public int getTroops() {
		return troops;
	}
	
	protected void setTroops(int troops) {
		this.troops = troops;
	}
	
	public Owner getOwner() {
		return owner;
	}
	
	protected void setOwner(Owner owner) {
		this.owner = owner;
	}

	@Override
	public int hashCode() {
		return 13*id;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Field)) {
			return false;
		}
		
		return id == ((Field)o).id;
	}
}
