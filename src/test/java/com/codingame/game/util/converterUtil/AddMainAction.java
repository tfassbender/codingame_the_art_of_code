package com.codingame.game.util.converterUtil;

public class AddMainAction implements ConvertAction {
	
	private String classname;
	
	public AddMainAction(String[] args) {
		if (args.length == 1) {
			classname = args[0];
		}
		else {
			classname = "Player";
		}
	}

	public String processAction(String str) {
		String main = "\tpublic static void main(String[] args) {" + "\n" +
			"\t\t" + classname + " p = new " + classname + "();" + "\n" +
			"\t\tp.play();" + "\n" +
		"\t}" + "\n";
		return main+"\n"+str;
	}

}
