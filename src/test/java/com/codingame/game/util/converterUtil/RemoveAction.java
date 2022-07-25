package com.codingame.game.util.converterUtil;

public class RemoveAction implements ConvertAction {

	String toRemove[];
	
	public RemoveAction(String toRemove[]) {
		this.toRemove = toRemove;
	}
	
	public String processAction(String str) {
		for (String next : toRemove) {
			str = str.replace(next, "");
		}
		
		return str;
	}

}
