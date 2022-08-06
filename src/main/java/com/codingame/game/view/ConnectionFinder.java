package com.codingame.game.view;

import com.codingame.game.util.Vector2D;

/**
 * Class to decide, whether a connection/bullet should use a direct way or a way over the borders. This Class also
 * delivers the the intermediate points, that lie on the border, which are used for an indirect way.
 */
public class ConnectionFinder {
	
	private static final double DIRECT_CONNECTION_BONUS = 0.9; // should be between [0; 1], with 0 always preferring the direct connection
	private static final int TOP = 0, RIGHT = 1, BOTTOM = 2, LEFT = 3;
	
	// Given in constructor
	private Vector2D field1, field2;
	private int width, height;
	private Vector2D offset;
	
	// Results of the computation
	private boolean isDirect;
	private Vector2D inter1, inter2;
	private boolean warpLeftRight;
	
	public ConnectionFinder(int width, int height, int xoff, int yoff, Vector2D field1, Vector2D field2) {
		offset = new Vector2D(xoff, yoff);
		this.field1 = field1.sub(offset);
		this.field2 = field2.sub(offset);
		
		this.height = height;
		this.width = width;
		
		findShortestConnection();
	}
	
	public boolean shouldTroopsFaceRight() {
		boolean faceRight = field1.x <= (field2.x + 1e-4);
		
		if (warpLeftRight)
			faceRight = !faceRight;
		
		return faceRight;
	}
	
	public Vector2D getIntersection1() {
		return inter1;
	}
	
	public Vector2D getIntersection2() {
		return inter2;
	}
	
	public boolean isDirect() {
		return isDirect;
	}
	
	// **********************************************************************
	// *** private methods
	// **********************************************************************
	
	private void findShortestConnection() {
		double directDist = field1.distance(field2) * DIRECT_CONNECTION_BONUS;
		double minIndirectDist = Double.MAX_VALUE;
		int minDirIndirect = -1;
		
		for (int i = 0; i < 4; i++) {
			// skip top/bottom tests, if both points lie on the same half
			if (!isBorderIsLeftOrRight(i) && ((field1.y <= height/2) == (field2.y <= height/2))) continue;

			// skip left/right tests, if both points lie on the same half
			if (isBorderIsLeftOrRight(i) && ((field1.x <= width/2) == (field2.x <= width/2))) continue;
			
			Vector2D imaginaryField = shortcut(field2, i);
			double dist = field1.distance(imaginaryField);
			
			if (dist < minIndirectDist) {
				minIndirectDist = dist;
				minDirIndirect = i;
			}
		}
		
		if (directDist < minIndirectDist) {
			isDirect = true;
			warpLeftRight = false;
		} else {
			isDirect = false;
			warpLeftRight = isBorderIsLeftOrRight(minDirIndirect);
			
			inter1 = getIntersectionFor(field1, field2, minDirIndirect).add(offset);
			inter2 = getIntersectionFor(field2, field1, getOpposideBorder(minDirIndirect)).add(offset);
		}
	}
	
	/**
	 * Estimates the cut of the line (start + t * end) to a given border.
	 */
	private Vector2D getIntersectionFor(Vector2D start, Vector2D end, int border) {
		Vector2D imaginaryField = shortcut(end, border);
		Vector2D dir = start.vectorTo(imaginaryField);
		double t = 0;
		
		if (border == TOP) {
			t = -start.y/dir.y;
		} else if (border == RIGHT) {
			t = -start.x/dir.x;
		} else if (border == BOTTOM) {
			t = (height-start.y)/dir.y;
		} else { // LEFT
			t = (width-start.x)/dir.x;
		}
		
		return start.add(dir.mult(t));
	}
	
	/**
	 * Estimates an imaginary position of a point, that is obtained, by repeating the map
	 * on a given border.
	 */
	private Vector2D shortcut(Vector2D f, int border) {
		if (border == TOP) {
			return new Vector2D(f.x, -(height-f.y));
		} else if (border == RIGHT) {
			return new Vector2D(-(width-f.x), f.y);
		} else if (border == BOTTOM) {
			return new Vector2D(f.x, f.y+height);
		} else { // LEFT
			return new Vector2D(f.x+width, f.y);
		}
	}
	
	private boolean isBorderIsLeftOrRight(int border) {
		return (border % 2) == 1;
	}
	
	private int getOpposideBorder(int border) {
		if (border == TOP) {
			return BOTTOM;
		} else if (border == RIGHT) {
			return LEFT;
		} else if (border == BOTTOM) {
			return TOP;
		} else { // LEFT
			return RIGHT;
		}
	}
}
