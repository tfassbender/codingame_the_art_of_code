package com.codingame.game.view;

import com.codingame.game.util.Vector2D;

public class ConnectionFinder {

	private Vector2D field1, field2;
	private int width, height;
	private Vector2D offset;
	boolean isDirect = false;
	private Vector2D inter1, inter2;
	
	public ConnectionFinder(int width, int height, int xoff, int yoff, Vector2D field1, Vector2D field2) {
		offset = new Vector2D(xoff, yoff);
		this.field1 = field1.sub(offset);
		this.field2 = field2.sub(offset);
		
		this.height = height;
		this.width = width;
		
		findShortestConnection();
	}
	
	public boolean isDirect() {
		return isDirect;
	}
	
	public Vector2D getIntersection1() {
		return inter1;
	}
	
	public Vector2D getIntersection2() {
		return inter2;
	}
	
	private void findShortestConnection() {
		// prefer direct connections a bit more (0.9)
		double directDist = field1.distance(field2)*0.9;
		double minInderectDist = Double.MAX_VALUE;
		int minDirInderect = -1;
		
		for (int i = 0; i < 4; i++) {
			if (i%2 == 0 && ((field1.y <= height/2) == (field2.y <= height/2))) continue;
			if (i%2 == 1 && ((field1.x <= width/2) == (field2.x <= width/2))) continue;
			
			Vector2D imaginaryField = shortcut(field2, i);
			double dist = field1.distance(imaginaryField);
			
			if (dist < minInderectDist) {
				minInderectDist = dist;
				minDirInderect = i;
			}
		}
		
		if (directDist < minInderectDist) {
			isDirect = true;
		} else {
			isDirect = false;
			
			System.out.println(minDirInderect);
			
			inter1 = getIntersectionFor(field1, field2, minDirInderect).add(offset);
			inter2 = getIntersectionFor(field2, field1, (2+minDirInderect)%4).add(offset);
		}
	}
	
	private Vector2D getIntersectionFor(Vector2D start, Vector2D end, int side) {
		Vector2D imaginaryField = shortcut(end, side);
		Vector2D dir = start.vectorTo(imaginaryField);
		double t = 0;
		
		// 0: cut with top
		// 1: cut with right
		// 2: cut with bottom
		// 3: cut with left
		
//		side = (side+2)%4;
		
		if (side == 0) {
			t = -start.y/dir.y;
		} else if (side == 1) {
			t = -start.x/dir.x;
		} else if (side == 2) {
			t = (height-start.y)/dir.y;
		} else {
			t = (width-start.x)/dir.x;
		}
		
		System.out.println("start: "+start+"\tdir: "+dir+"\tt: "+t);
		
		return start.add(dir.mult(t));
	}
	
	private Vector2D shortcut(Vector2D f, int side) {
		// 0: mirror on top
		// 1: mirror on right
		// 2: mirror on bottom
		// 3: mirror on left
		
		if (side == 0) {
			return new Vector2D(f.x, -(height-f.y));
		} else if (side == 1) {
			return new Vector2D(-(width-f.x), f.y);
		} else if (side == 2) {
			return new Vector2D(f.x, f.y+height);
		} else {
			return new Vector2D(f.x+width, f.y);
		}
	}
}
