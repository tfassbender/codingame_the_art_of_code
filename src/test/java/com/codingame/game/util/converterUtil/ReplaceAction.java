package com.codingame.game.util.converterUtil;

public class ReplaceAction implements ConvertAction {

	String regex;
	String replaceWith;
	
	public ReplaceAction(String regex, String replaceWith) {
		this.regex = regex;
		this.replaceWith = replaceWith;
	}
	
	public String processAction(String str) {
		str = str.replace(regex, replaceWith);
		
		return str;
	}

}
