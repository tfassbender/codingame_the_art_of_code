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
		owner = Owner.NEUTRAL;
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
}
