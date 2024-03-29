package com.codingame.game.view;

import java.util.ArrayList;
import java.util.List;

import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;

/**
 * Class to unify route handling (whether they are direct or with a warp along the borders)
 */
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
	
	/**
	 * Estimates the least amount of positions, that is required for interpolation/animation, to
	 * complete the relative travel along the route. If the route is trivial (that is, a direct
	 * connection between the start and end), only a position will be returned. 
	 * 
	 * @param relStart Relative start point along the track [0; 1]
	 * @param relEnd Relative end point along the track [0; 1]
	 * @return List of Pair<t, position>: 
	 * 		   t:= relative time [0; 1] along the travel (last t is always 1)
	 * 		   position:= position of the Entity at the relative time point t
	 */
	public List<Pair<Double, Vector2D>> getTravelWithRelativeTimestamps(double relStart, double relEnd) {
		List<Pair<Double, Vector2D>> travelPoints = new ArrayList<Pair<Double, Vector2D>>();
		int startPart = getStitchIndex(relStart);
		int endPart = getStitchIndex(relEnd);
		boolean forward = relStart < relEnd;
		double t = 0;
		double warpStepSize = 1e-3;
		
		if (startPart == endPart) {
			// one pair only (end)
			
			t = 1;
			travelPoints.add(Pair.of(t, estimatePosition(relEnd)));
		} else if (Math.abs(endPart-startPart) == 1) {
			// three pairs (intermediate1, intermediate2, end)
			
			t = estimateWarpTimestamp(relStart, relEnd, startPart, endPart);
			travelPoints.add(Pair.of(t, getPosition(startPart, !forward)));
			
			t += warpStepSize; // warp should be (almost) instant
			travelPoints.add(Pair.of(t, getPosition(endPart, forward)));
			
			t = 1;
			travelPoints.add(Pair.of(t, estimatePosition(relEnd)));
		} else {
			throw new RuntimeException("The algorithm won't support 3 way stitches...");
		}
		
		return travelPoints;
	}
	
	/**
	 * Estimates the relative time stamp t [0; 1], where the warp has to occur, given, that the
	 * Entity moves at the same speed all the time.
	 */
	private double estimateWarpTimestamp(double relStart, double relEnd, int startIdx, int endIdx) {
		boolean forward = relStart < relEnd;
		double travelDist1 = estimatePosition(relStart).distance(getPosition(startIdx, !forward));
		double travelDist2 = getPosition(endIdx, forward).distance(estimatePosition(relEnd));
		
		return travelDist1 / (travelDist1 + travelDist2);
	}
	
	/**
	 * Helper function, to access the end or the start of a stitch.
	 */
	private Vector2D getPosition(int idx, boolean startPoint) {
		Pair<Vector2D, Vector2D> stitch = stitches.get(idx);
		
		return startPoint ? stitch.getKey() : stitch.getValue();
	}
	
	/**
	 * Estimate the coordinates (x,y) of the relative position [0; 1] on the route. 
	 */
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
	
	/**
	 * Estimate the index of the stitch, where the relative position [0; 1] lies on. 
	 */
	private int getStitchIndex(double relativePosition) {
		double totalLength = estimateTotalLength();
		double traveled = totalLength * relativePosition;
		double sumLength = 0;
		
		if (relativePosition <= 0) {
			return 0;
		} else if (relativePosition >= 1) {
			return stitches.size() - 1;
		}
		
		for (int i = 0; i < stitches.size(); i++) {
			Pair<Vector2D, Vector2D> stitch = stitches.get(i);
			double length = stitch.getKey().distance(stitch.getValue());
			
			sumLength += length;
			
			// do we overtake the relative position?
			if (sumLength > traveled) {
				return i;
			}
		}
		
		return stitches.size() - 1;
	}
	
	private double estimateTotalLength() {
		return stitches.stream().mapToDouble(pair -> pair.getKey().distance(pair.getValue())).sum();
	}
};