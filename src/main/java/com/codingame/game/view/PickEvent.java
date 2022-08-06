package com.codingame.game.view;

import com.codingame.game.core.Owner;

/**
 * Container to store the PICK information that is relevant for the view.
 */
public class PickEvent {

	public final int targetId;
	public final Owner owner;
	public final boolean denied;
	
	public PickEvent(int targetId, Owner owner, boolean denied) {
		this.targetId = targetId;
		this.owner = owner;
		this.denied = denied;
	}
	
}
