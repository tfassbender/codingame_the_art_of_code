package com.codingame.game.view.map;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.codingame.game.util.Vector2D;

/**
 * Calculates the position of the nodes in a graph, by using a spring-embedder algorithm.
 * See https://i11www.iti.kit.edu/_media/teaching/winter2016/graphvis/graphvis-ws16-v6.pdf
 * 
 * NOTE: The positions of the nodes will be changed in place as a side effect!
 * 
 * Inputs:
 * - The undirected graph with initial positions of the nodes
 * - The number of iterations (optional; default is 100)
 * - The 'idealSpringLength' for two connected nodes (optional)
 * - The 'idealClusterDistance' for two nodes in the same cluster (optional)
 * - A constant 'delta' that is used as a factor for the change in the position of each node depending on the force (optional)
 * - A constant 'deltaCooldown' that is used as cooldown factor for 'delta' (must be between 0 and 1; optional)
 * - A constant 'repulsiveForce' as factor for the force that tries to divide the fields from each other (optional)
 * - A constant 'springForce' as factor for the force that tries to gather connected nodes closer to each other (optional)
 * - A constant 'clusterForce' as factor for the force that tries to gather nodes of a cluster closer to each other (optional)
 * - The 'bounds' in which the nodes have to be arranged (optional; default is no bounds)  
 * 
 *  Outputs:
 *  The positioned nodes as a {@link Set} of {@link Positioned} objects.
 */
public class GraphPlacement<T> {
	
	//************************************************************************
	//*** input values
	//************************************************************************
	
	// the original graph from the input
	private Graph<T> graph;
	private Set<Positioned<T>> fields;
	
	private Map<Positioned<T>, Set<Positioned<T>>> connectedFields;
	private Map<Positioned<T>, Set<Positioned<T>>> connectedClusters;
	
	//************************************************************************
	//*** parameters
	//************************************************************************
	
	private int iterations = 100;
	
	private float idealSpringLength = 1f; // ideal distance between two connected nodes
	private float idealClusterDistance = 1.5f; // ideal distance between two nodes in the same cluster
	
	private float delta = 1f; // factor for the change of positions of the nodes
	private float deltaCooldown = 1f; // factor for the cooldown of delta, so the positions stabilise (must be between 0 and 1)
	
	private float repulsiveForce = 1f; // factor for the force that tries to divide the fields from each other
	private float springForce = 1f; // factor for the force that tries to gather connected nodes closer to each other
	private float clusterForce = 0.5f; // factor for the force that tries to gather nodes of a cluster closer to each other
	
	private boolean useBounds = false;
	private float xMin = 0;
	private float yMin = 0;
	private float xMax = 0;
	private float yMax = 0;
	
	//************************************************************************
	//*** algorithm
	//************************************************************************
	
	public GraphPlacement(Graph<T> graph) {
		this.graph = graph;
		this.fields = graph.getFields();
		
		calculateConnectedFields();
		calculateConnectedClusters();
	}
	
	private void calculateConnectedFields() {
		connectedFields = new HashMap<>();
		for (Positioned<T> field : fields) {
			for (Positioned<T> other : fields) {
				if (field != other && graph.isFieldsConnected(field, other)) {
					connectedFields.computeIfAbsent(field, x -> new HashSet<>()).add(other);
				}
			}
		}
	}
	
	private void calculateConnectedClusters() {
		Set<Set<Positioned<T>>> clusters = new HashSet<>();
		for (Positioned<T> field : fields) {
			boolean clusterFound = false;
			
			// find an existing cluster to add the field
			for (Set<Positioned<T>> cluster : clusters) {
				Positioned<T> fieldInCluster = cluster.stream().findFirst().get(); // clusters cannot be empty
				if (graph.isFieldsInSameCluster(field, fieldInCluster)) {
					cluster.add(field);
					clusterFound = true;
				}
			}
			
			if (!clusterFound) {
				// create a new cluster
				Set<Positioned<T>> cluster = new HashSet<>();
				cluster.add(field);
				clusters.add(cluster);
			}
		}
		
		// map each field to it's cluster
		connectedClusters = new HashMap<>();
		for (Set<Positioned<T>> cluster : clusters) {
			for (Positioned<T> field : cluster) {
				connectedClusters.put(field, cluster);
			}
		}
	}
	
	public Set<Positioned<T>> positionFields() {
		float delta_t = delta;
		
		Map<Positioned<T>, Vector2D> displacementForces = new HashMap<>();
		for (int i = 0; i < iterations; i++) {
			// collect all displacement forces, so the fields are not moved within the iteration step 
			for (Positioned<T> field : fields) {
				displacementForces.put(field, resultingDisplacementVector(field));
			}
			
			// apply the displacement forces to move the fields
			for (Positioned<T> field : fields) {
				Vector2D displacementVector = displacementForces.get(field).mult(delta_t);
				field.setPosition(field.pos().add(displacementVector));
				
				delta_t *= deltaCooldown;
			}
			
			if (useBounds) {
				// move nodes back into the given bounds, if they were moved out of the bounds
				for (Positioned<T> field : fields) {
					Vector2D truncated = field.pos();
					if (field.pos().x < xMin) {
						truncated.x = xMin;
					}
					else if (field.pos().x > xMax) {
						truncated.x = xMax;
					}
					if (field.pos().y < yMin) {
						truncated.y = yMin;
					}
					else if (field.pos().y > yMax) {
						truncated.y = yMax;
					}
					field.setPosition(truncated);
				}
			}
		}
		
		return fields;
	}
	
	private Vector2D resultingDisplacementVector(Positioned<T> field) {
		Vector2D resultingDisplacementVector = new Vector2D();
		
		// repulsive force between not connected nodes
		for (Positioned<T> other : fields) {
			if (field != other && !graph.isFieldsConnected(field, other)) {
				resultingDisplacementVector = resultingDisplacementVector.add(repulsiveForceVector(field, other));
			}
		}
		
		// attractive force between connected nodes
		for (Positioned<T> connected : connectedFields.get(field)) {
			resultingDisplacementVector = resultingDisplacementVector.add(attractiveSpringForceVector(field, connected));
		}
		
		// attractive force between nodes in a cluster
		for (Positioned<T> clusterField : connectedClusters.get(field)) {
			if (field != clusterField) {
				resultingDisplacementVector = resultingDisplacementVector.add(attractiveClusterForceVector(field, clusterField));
			}
		}
		
		return resultingDisplacementVector;
	}
	
	private Vector2D repulsiveForceVector(Positioned<T> field1, Positioned<T> field2) {
		if (graph.isFieldsConnected(field1, field2)) {
			return Vector2D.NULL_VEC;
		}
		
		return field1.pos().vectorTo(field2.pos()).mult(repulsiveForce / field1.pos().distance2(field2.pos()));
	}
	
	private Vector2D attractiveSpringForceVector(Positioned<T> field1, Positioned<T> field2) {
		if (!graph.isFieldsConnected(field1, field2)) {
			return Vector2D.NULL_VEC;
		}
		
		return field2.pos().vectorTo(field1.pos()).mult(springForce * Math.log10(field1.pos().distance(field2.pos())) / idealSpringLength);
	}
	
	private Vector2D attractiveClusterForceVector(Positioned<T> field1, Positioned<T> field2) {
		if (!graph.isFieldsInSameCluster(field1, field2)) {
			return Vector2D.NULL_VEC;
		}
		
		return field2.pos().vectorTo(field1.pos()).mult(clusterForce * Math.log10(field1.pos().distance(field2.pos())) / idealSpringLength);
	}
	
	//************************************************************************
	//*** getters and setters for parameters
	//************************************************************************
	
	public int getIterations() {
		return iterations;
	}
	
	public void setIterations(int iterations) {
		if (iterations < 1) {
			throw new IllegalArgumentException("Iterations must be at least 1");
		}
		this.iterations = iterations;
	}
	
	public float getIdealSpringLength() {
		return idealSpringLength;
	}
	
	public void setIdealSpringLength(float idealSpringLength) {
		if (idealSpringLength < 0) {
			throw new IllegalArgumentException("The ideals spring length cannot be below 0");
		}
		this.idealSpringLength = idealSpringLength;
	}
	
	public float getIdealClusterDistance() {
		return idealClusterDistance;
	}
	
	public void setIdealClusterDistance(float idealClusterDistance) {
		if (idealClusterDistance < 0) {
			throw new IllegalArgumentException("The ideals cluster distance cannot be below 0");
		}
		this.idealClusterDistance = idealClusterDistance;
	}
	
	public float getDelta() {
		return delta;
	}
	
	public void setDelta(float delta) {
		if (delta <= 0) {
			throw new IllegalArgumentException("Delta must be greater than 0");
		}
		this.delta = delta;
	}
	
	public float getDeltaCooldown() {
		return deltaCooldown;
	}
	
	public void setDeltaCooldown(float deltaCooldown) {
		if (deltaCooldown <= 0 || deltaCooldown > 1) {
			throw new IllegalArgumentException("The delta cooldown must be between 0 (exclusive) and 1 (inclusive)");
		}
		this.deltaCooldown = deltaCooldown;
	}
	
	public float getRepulsiveForce() {
		return repulsiveForce;
	}
	
	public void setRepulsiveForce(float repulsiveForce) {
		if (repulsiveForce <= 0) {
			throw new IllegalArgumentException("The repulsive force must be greater than 0");
		}
		this.repulsiveForce = repulsiveForce;
	}
	
	public float getSpringForce() {
		return springForce;
	}
	
	public void setSpringForce(float springForce) {
		if (springForce <= 0) {
			throw new IllegalArgumentException("The spring force must be greater than 0");
		}
		this.springForce = springForce;
	}
	
	public float getClusterForce() {
		return clusterForce;
	}
	
	public void setClusterForce(float clusterForce) {
		if (clusterForce <= 0) {
			throw new IllegalArgumentException("The cluster force must be greater than 0");
		}
		this.clusterForce = clusterForce;
	}
	
	public void setBounds(float xMin, float yMin, float xMax, float yMax) {
		if (xMin >= xMax) {
			throw new IllegalArgumentException("xMin must be below xMax");
		}
		if (yMin >= yMax) {
			throw new IllegalArgumentException("yMin must be below yMax");
		}
		this.xMin = xMin;
		this.yMin = yMin;
		this.xMax = xMax;
		this.yMax = yMax;
		useBounds = true;
	}
	
	public void resetBounds() {
		useBounds = false;
	}
}
