package com.codingame.game.core;

import java.util.Map;
import java.util.Set;

import com.codingame.game.util.Vector2D;

/**
 * A set of (connected) fields, that form a region.
 */
public class Region {
	
	public final Set<Field> fields;
	public final int bonusTroops;
	public final int id;
	
	private static int REGION_ID_CNT = 0;
	
	// TODO maybe add a color or something to identify the region on the map
	
	public Region(Set<Field> fields, int bonusTroops) {
		this.fields = fields;
		this.bonusTroops = bonusTroops;
		
		this.id = REGION_ID_CNT++; // TODO find better names
	}
	
	public boolean isConqueredBy(Owner owner) {
		return fields.stream().allMatch(field -> field.getOwner() == owner);
	}
	
	public int getBonusTroops() {
		return bonusTroops;
	}
	
	public String getId() {
		return "R"+id;
	}
	
	public double distToPoint(Vector2D point, Map<Field, Vector2D> fieldPositions, int norm) {
		return fields.stream().mapToDouble(f -> fieldPositions.get(f).sub(point).length(norm)).min().getAsDouble();
	}
}
