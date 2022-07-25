package com.codingame.game.util.converterUtil;

import java.util.ArrayList;

public class Options {

	public String commandArgs[];
	public boolean global;
	public boolean trim;

	public Options(String fullCommandArgs[]) {
		ArrayList<String> commandArgs = new ArrayList<String>();
		
		for (String next : fullCommandArgs) {
			if (next.startsWith("-")) {
				switch (next.substring(1)) {
				case "global": global = true; break;
				case "trim": trim = true; break;
				default:
					throw new IllegalArgumentException(
							"Invalid converter option: " + next);
				}
			} else {
				commandArgs.add(next);
			}
		}
		this.commandArgs = new String[commandArgs.size()];
		commandArgs.toArray(this.commandArgs);
	}

}
