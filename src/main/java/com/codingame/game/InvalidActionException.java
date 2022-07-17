package com.codingame.game;

public class InvalidActionException extends Exception {
	
	private static final long serialVersionUID = -8185589153224401564L;
	
	public InvalidActionException(String message) {
		super(message);
	}
}