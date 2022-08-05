package com.codingame.game.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.codingame.game.core.Field;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;

public class TroopNavigator {

	public class Route {
		
		private List<Pair<Vector2D, Vector2D>> stitches = new ArrayList<Pair<Vector2D, Vector2D>>();
		
		public Route(Vector2D start, Vector2D end) {
			stitches.add(Pair.of(start, end));
		}
		
		public Route(Vector2D start, Vector2D intermediate1, Vector2D intermediate2, Vector2D end) {
			stitches.add(Pair.of(start, intermediate1));
			stitches.add(Pair.of(intermediate2, end));
		}
		
		public boolean isTrivialRoute() {
			return stitches.size() <= 1;
		}
		
		public List<Pair<Double, Vector2D>> getTravelWithRelTimestamps(double relStart, double relEnd) {
			List<Pair<Double, Vector2D>> travelPoints = new ArrayList<Pair<Double, Vector2D>>();
			int startPart = getStitchIndex(relStart);
			int endPart = getStitchIndex(relEnd);
			boolean forward = relStart < relEnd;
			double t = 0;
			double warpStepSize = 1e-3;
			
			if (startPart == endPart) {
				// one pair only (end)
				
				t = 1;
				travelPoints.add(Pair.of(t, getPosition(startPart, !forward)));
			} else if (Math.abs(endPart-startPart) == 1) {
				// three pairs (intermediate1, intermediate2, end)
				
				t = estimateWarpTimestamp(relStart, relEnd, startPart, endPart);
				travelPoints.add(Pair.of(t, getPosition(startPart, !forward)));
				
				t += warpStepSize; // warp should be instant
				travelPoints.add(Pair.of(t, getPosition(endPart, forward)));
				
				t = 1;
				travelPoints.add(Pair.of(t, getPosition(endPart, !forward)));
			} else {
				throw new RuntimeException("The algorithm won't support 3 way stitches...");
			}
			
			return travelPoints;
		}
		
		private double estimateWarpTimestamp(double relStart, double relEnd, int startIdx, int endIdx) {
			boolean forward = relStart < relEnd;
			double travelDist1 = estimatePosition(relStart).distance(getPosition(startIdx, !forward));
			double travelDist2 = getPosition(endIdx, forward).distance(estimatePosition(relEnd));
			
			return travelDist1 / (travelDist1 + travelDist2);
		}
		
		private Vector2D getPosition(int idx, boolean startPoint) {
			Pair<Vector2D, Vector2D> stitch = stitches.get(idx);
			
			return startPoint ? stitch.getKey() : stitch.getValue();
		}
		
		public Vector2D estimatePosition(double relPosition) {
			double totalLength = estimateTotalLength();
			double traveled = totalLength * relPosition;
			double sumLength = 0;
			
			if (relPosition <= 0) {
				return stitches.get(0).getKey();
			} else if (relPosition >= 1) {
				return stitches.get(stitches.size()-1).getValue();
			}
			
			for (int i = 0; i < stitches.size(); i++) {
				Pair<Vector2D, Vector2D> stitch = stitches.get(i);
				double length = stitch.getKey().distance(stitch.getValue());
				
				sumLength += length;
				
				if (sumLength > traveled) {
					double relOffset = (sumLength-length)/totalLength;
					
					return stitch.getKey().add(stitch.getKey().vectorTo(stitch.getValue()).mult(stitches.size()*(relPosition - relOffset)));
				}
			}
			
			return stitches.get(stitches.size()-1).getValue();
		}
		
		private double estimateTotalLength() {
			return stitches.stream().mapToDouble(pair -> pair.getKey().distance(pair.getValue())).sum();
		}
		
		private int getStitchIndex(double relPosition) {
			double totalLength = estimateTotalLength();
			double traveled = totalLength * relPosition;
			double sumLength = 0;
			
			if (relPosition <= 0) {
				return 0;
			} else if (relPosition >= 1) {
				return stitches.size() - 1;
			}
			
			for (int i = 0; i < stitches.size(); i++) {
				Pair<Vector2D, Vector2D> stitch = stitches.get(i);
				double length = stitch.getKey().distance(stitch.getValue());
				
				sumLength += length;
				
				if (sumLength > traveled) {
					return i;
				}
			}
			
			return stitches.size() - 1;
		}
	};
	
	private Map<Pair<Field, Field>, Route> connectionPositions;
	
	public TroopNavigator() {
		connectionPositions = new HashMap<Pair<Field, Field>, Route>();
	}
	
	public void addOnePartConnection(Pair<Field, Field> key, Vector2D start, Vector2D end) {
		connectionPositions.put(key, new Route(start, end));
		connectionPositions.put(key.swap(), new Route(end, start));
	}
	
	public void addTwoPartConnection(Pair<Field, Field> key, Vector2D start, Vector2D intermediate1, Vector2D intermediate2, Vector2D end) {
		connectionPositions.put(key, new Route(start, intermediate1, intermediate2, end));
		connectionPositions.put(key.swap(), new Route(end, intermediate2, intermediate1, start));
	}
	
	public Route getRoute(Pair<Field, Field> connection) {
		return connectionPositions.get(connection);
	}
}
