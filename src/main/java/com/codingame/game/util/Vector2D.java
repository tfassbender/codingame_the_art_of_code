package com.codingame.game.util;

public class Vector2D implements Cloneable {
	
	public double x;
	public double y;
	
	public static final Vector2D NAN_VEC = new Vector2D(Double.NaN, Double.NaN);
	public static final Vector2D NULL_VEC = new Vector2D(0, 0);
	
	/*public enum Axis {
		X,
		Y;
	}*/
	
	public Vector2D() {
		
	}
	
	/**
	 * Crate a new Vector2D with x and y components.
	 */
	public Vector2D(double x, double y) {
		this.x = x;
		this.y = y;
	}
	public Vector2D(double... val) {
		if (val.length != 2) {
			//throw new LinearAlgebraException("A 2D Vector has 2 entries.");
		}
		x = val[0];
		y = val[1];
	}
	/**
	 * Create a Vector2D by an angle (in degree).
	 * An angle of 0° results in (x, y) = (1, 0); 90° in (x, y) = (0, 1); ...
	 * The resulting vector has a length of 1.
	 * 
	 * @param angleDegree
	 * 		The angle of the new vector in degree.
	 */
	public Vector2D(double angleDegree) {
		this(Math.cos(angleDegree * Math.PI / 180), Math.sin(angleDegree * Math.PI / 180));
	}
	
	private Vector2D(Vector2D clone) {
		this.x = clone.x;
		this.y = clone.y;
	}
	
	/**
	 * Clone this Vector2D object.
	 */
	@Override
	public Vector2D clone() {
		return new Vector2D(this);
	}
	
	@Override
	public String toString() {
		return "Vector2D[x: " + x + " y: " + y + "]";
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Vector2D) {
			Vector2D v = (Vector2D) obj;
			return Math.abs(x-v.x) < 1e-8 && Math.abs(y-v.y) < 1e-8;
		}
		return false;
	}
	
	/**
	 * Get this vector as 2D-Array.
	 */
	public double[] asArray() {
		return new double[] {x, y};
	}
	
	/**
	 * The (euclidean) length of the Vector.
	 */
	public double length() {
		return Math.hypot(x, y);
	}
	/**
	 * The length of this vector in a given norm.
	 * 
	 * @param norm
	 * 		The norm of the vector length.
	 * 
	 * @return
	 * 		The length of this vector in the given norm.
	 */
	public double length(int norm) {
		if (norm == Integer.MAX_VALUE) {
			return Math.max(x, y);
		}
		return Math.pow(Math.pow(x, norm) + Math.pow(y, norm), 1.0/norm);
	}
	
	/**
	 * Rotate the Vector an angle (in degrees) resulting in a new Vector that is returned.
	 * 
	 * @param degrees
	 * 		The angle to return the vector.
	 * 
	 * @return
	 *		The new created vector.
	 */
	public Vector2D rotate(double degrees) {
		return new Vector2D(getAngle() + degrees).setLength(length());
	}
	
	/**
	 * Project the vector given as parameter on this vector.
	 * 
	 * @param vec
	 * 		The vector that is to be projected on this vector.
	 * 
	 * @return
	 * 		The projected vector.
	 */
	public Vector2D project(Vector2D vec) {
		return mult(scalar(vec) / Math.pow(length(), 2));
	}
	
	/**
	 * Add another Vector2D to this vector resulting in a new Vector that is returned.
	 * 
	 * @param vec
	 * 		The vector added to this vector.
	 * 
	 * @return
	 * 		The new created vector.
	 */
	public Vector2D add(Vector2D vec) {
		return new Vector2D(x + vec.x, y + vec.y);
	}
	/**
	 * Subtract another Vector3D from this vector resulting in a new Vector that is returned.
	 * 
	 * @param vec
	 * 		The vector subtracted from this vector.
	 * 
	 * @return
	 * 		The new created vector.
	 */
	public Vector2D sub(Vector2D vec) {
		return new Vector2D(x - vec.x, y - vec.y);
	}
	/**
	 * Multiply this vector with a scalar resulting in a new Vector that is returned.
	 * 
	 * @param scalar
	 * 		The scalar to multiply this vector with.
	 * 
	 * @return
	 * 		The new created vector.
	 */
	public Vector2D mult(double scalar) {
		return new Vector2D(x * scalar, y * scalar);
	}
	
	/**
	 * Check whether this vector is linearly dependent to the parameter vector.
	 * 
	 * @param vec
	 * 		The checked vector.
	 * 
	 * @return
	 * 		True if the vectors are linearly dependent. False otherwise.
	 */
	public boolean isLinearlyDependent(Vector2D vec) {
		double t1 = (x == 0 ? 0 : vec.x/x);
		double t2 = (y == 0 ? 0 : vec.y/y);
		return Math.abs(t1-t2) < 1e-5 && t1 != 0;//all parameters t are equal and != 0
	}
	
	/**
	 * Check whether the parameter vectors are linearly dependent.
	 * 
	 * @param vectors
	 * 		The vectors that are checked.
	 * 
	 * @return
	 * 		True if the vectors are linearly dependent. False otherwise.
	 */
	public boolean isLinearlyDependentVectors(Vector2D... vectors) {
		if (vectors.length < 2) {
			return false;
		}
		else if (vectors.length > 2) {
			//3 or more vectors in the R^2 are always linearly dependent
			return true;
		}
		else {
			return vectors[0].isLinearlyDependent(vectors[1]);
		}
	}
	
	/**
	 * Calculate the scalar product of this vector and the parameter vector.
	 * 
	 * @param vec
	 * 		The vector to calculate the scalar with this vector.
	 * 
	 * @return
	 * 		The scalar of the vectors.
	 */
	public double scalar(Vector2D vec) {
		return this.x * vec.x + this.y * vec.y;
	}
	
	/**
	 * Create a new vector with the same direction but a different length as this vector.
	 * 
	 * @param length
	 * 		The length of the new vector.
	 * 
	 * @return
	 * 		The new vector with a new length.
	 */
	public Vector2D setLength(double length) {
		double len = length();
		return new Vector2D(x * length / len, y * length / len);
	}
	
	/**
	 * Get the distance of this point's position vector to another point's position vector.
	 * 
	 * @param p
	 * 		The second point's position vector.
	 * 
	 * @return
	 * 		The distance between the points.
	 */
	public double distance(Vector2D p) {
		return Math.sqrt((this.x - p.x) * (this.x - p.x) + (this.y - p.y) * (this.y - p.y));
	}
	
	/**
	 * Change this vector to the new coordinates.
	 */
	public void move(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	/**
	 * Move a point's position vector in a direction (by a vector) and a distance.
	 * 
	 * @param p
	 * 		The direction vector.
	 * 
	 * @param distance
	 * 		The distance to move the new vector
	 * 
	 * @return
	 * 		The new created vector.
	 */
	public Vector2D moveTo(Vector2D p, double distance) {
		double d = distance(p);
		double dx = p.x - x;
		double dy = p.y - y;
		double coef = distance / d;
		return new Vector2D(x + dx * coef, y + dy * coef);
	}
	
	/**
	 * Get the angle of this vector.
	 * Angle: 0° is right ((x, y) = (1, 0)); on clockwise (degree)
	 */
	public double getAngle() {
		return ((Math.atan2(y, x) * 180 / Math.PI) + 720) % 360;
	}
	
	/**
	 *	Get the angle of this vector as radiant.
	 */
	public double getAngleRad() {
		return (Math.atan2(y, x) + 4 * Math.PI) % (2 * Math.PI);
	}
	
	/**
	 * Get the angle difference of this vector to another vector.
	 * 
	 * @param vec
	 * 		The other vector.
	 * 
	 * @return
	 * 		The angle difference of the two vectors (from 0° to 180°).
	 */
	public double getAngleDeltaTo(Vector2D vec) {
		double delta = Math.abs(getAngle() - vec.getAngle());
		if (delta > 180) {
			delta = 360 - delta;
		}
		return delta;
	}
	
	/**
	 * Get the vector from this point to another.
	 * 
	 * @param vec
	 * 		The point to which the vector is calculated.
	 * 
	 * @return
	 * 		The vector from this points position vector to the other point.
	 */
	public Vector2D vectorTo(Vector2D vec) {
		return new Vector2D(vec.x - x, vec.y - y);
	}
	
	/**
	 * Checks whether a point (by its position vector) is in a given range of this point.
	 * 
	 * @param p
	 * 		The point that is checked.
	 * 
	 * @param range
	 * 		The range used for the check.
	 * 
	 * @return
	 * 		True if the point is in the range of this point (distance <= range).
	 */
	public boolean isInRange(Vector2D p, double range) {
		return p != this && distance(p) <= range;
	}
	
	public double getX() {
		return x;
	}
	public double getY() {
		return y;
	}
}