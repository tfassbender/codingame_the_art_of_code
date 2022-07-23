package com.codingame.game.core;

import java.util.Set;

/**
 * A set of (connected) fields, that form a region.
 */
public class Region {
	
	public final Set<Field> fields;
	public final int bonusTroops;
	public final String id;
	
	// TODO maybe add a color or something to identify the region on the map
	
	public Region(Set<Field> fields, int bonusTroops) {
		this.fields = fields;
		this.bonusTroops = bonusTroops;
		
		this.id = "R1"; // TODO find better names
	}
	
	public boolean isConqueredBy(Owner owner) {
		return fields.stream().allMatch(field -> field.getOwner() == owner);
	}
	
	public int getBonusTroops() {
		return bonusTroops;
	}
	
	public String getId() {
		return id;
	}
}
