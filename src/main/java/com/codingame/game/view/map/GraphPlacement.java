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
 * - The 'idealNonAdjacentDistance' for two nodes that are not adjacent (optional)
 * - A constant 'delta' that is used as a factor for the change in the position of each node depending on the force (optional)
 * - A constant 'deltaCooldown' that is used as cooldown factor for 'delta' (must be between 0 and 1; optional)
 * - A constant 'repulsiveForce' as factor for the force that tries to divide the fields from each other (optional)
 * - A constant 'springForce' as factor for the force that tries to gather connected nodes closer to each other (optional)
 * - A constant 'clusterForce' as factor for the force that tries to gather nodes of a cluster closer to each other (optional)
 * - A constant 'maxForceFactor' as the maximum force that can be applied to a repulsive or attractive force vector in one step 
 *   of the iteration (1 means the distance between two nodes; optional)
 * - The 'bounds' in which the nodes have to be arranged (optional; default is no bounds)
 * - The 'variant' of the algorithm that is used (optional)
 * 
 *  Outputs:
 *  The positioned nodes as a {@link Set} of {@link Positioned} objects.
 */
public class GraphPlacement<T extends Positioned<?>> {
	
	public enum Variant {
		SPRING_EMBEDDER, // repulsive force between non adjacent nodes; attractive force between adjacent and cluster nodes
		FRUCHTERMAN_REINGOLD; // repulsive force between all nodes; attractive force between adjacent and cluster nodes (more based on distances)
	}
	
	private interface VariantForceCalculator<T> {
		
		public Vector2D resultingDisplacementVector(T field);
	}
	
	//************************************************************************
	//*** input values
	//************************************************************************
	
	// the original graph from the input
	private Graph<T> graph;
	private Set<T> fields;
	
	private Map<T, Set<T>> connectedFields;
	private Map<T, Set<T>> connectedClusters;
	
	//************************************************************************
	//*** parameters
	//************************************************************************
	
	private int iterations = 100;
	
	private float idealSpringLength = 1f; // ideal distance between two connected nodes
	private float idealClusterDistance = 1.5f; // ideal distance between two nodes in the same cluster
	private float idealNonAdjacentDistance = 3f; // ideal distance between two nodes that are not connected
	
	private float delta = 1f; // factor for the change of positions of the nodes
	private float deltaCooldown = 1f; // factor for the cooldown of delta, so the positions stabilise (must be between 0 and 1)
	
	private float repulsiveForce = 1f; // factor for the force that tries to divide the fields from each other
	private float springForce = 1f; // factor for the force that tries to gather connected nodes closer to each other
	private float clusterForce = 0.5f; // factor for the force that tries to gather nodes of a cluster closer to each other
	private float maxForceFactor = Float.MAX_VALUE; // the maximum force that can be applied to a repulsive or attractive force vector in one iteration step (1 means the distance between two nodes)
	
	private boolean useBounds = false;
	private float xMin = 0;
	private float yMin = 0;
	private float xMax = 0;
	private float yMax = 0;
	
	private Variant variant = Variant.SPRING_EMBEDDER;
	private VariantForceCalculator<T> variantImplementation = new SpringEnbedderForceCalculator();
	
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
		for (T field : fields) {
			for (T other : fields) {
				if (field != other && graph.isFieldsConnected(field, other)) {
					connectedFields.computeIfAbsent(field, x -> new HashSet<>()).add(other);
				}
			}
		}
	}
	
	private void calculateConnectedClusters() {
		Set<Set<T>> clusters = new HashSet<>();
		for (T field : fields) {
			boolean clusterFound = false;
			
			// find an existing cluster to add the field
			for (Set<T> cluster : clusters) {
				T fieldInCluster = cluster.stream().findFirst().get(); // clusters cannot be empty
				if (graph.isFieldsInSameCluster(field, fieldInCluster)) {
					cluster.add(field);
					clusterFound = true;
				}
			}
			
			if (!clusterFound) {
				// create a new cluster
				Set<T> cluster = new HashSet<>();
				cluster.add(field);
				clusters.add(cluster);
			}
		}
		
		// map each field to it's cluster
		connectedClusters = new HashMap<>();
		for (Set<T> cluster : clusters) {
			for (T field : cluster) {
				connectedClusters.put(field, cluster);
			}
		}
	}
	
	public Set<T> positionFields() {
		float delta_t = delta;
		
		Map<T, Vector2D> displacementForces = new HashMap<>();
		for (int i = 0; i < iterations; i++) {
			// collect all displacement forces, so the fields are not moved within the iteration step 
			for (T field : fields) {
				displacementForces.put(field, variantImplementation.resultingDisplacementVector(field));
			}
			
			// apply the displacement forces to move the fields
			for (T field : fields) {
				Vector2D displacementVector = displacementForces.get(field).mult(delta_t);
				field.setPosition(field.pos().add(displacementVector));
				
				delta_t *= deltaCooldown;
			}
			
			if (useBounds) {
				// move nodes back into the given bounds, if they were moved out of the bounds
				for (T field : fields) {
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
	
	public float getIdealNonAdjacentDistance() {
		return idealNonAdjacentDistance;
	}
	
	public void setIdealNonAdjacentDistance(float idealNonAdjacentDistance) {
		if (idealNonAdjacentDistance < 0) {
			throw new IllegalArgumentException("The ideals non-adjacent distance cannot be below 0");
		}
		this.idealNonAdjacentDistance = idealNonAdjacentDistance;
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
		if (repulsiveForce < 0) {
			throw new IllegalArgumentException("The repulsive force must be greater than or equal to 0");
		}
		this.repulsiveForce = repulsiveForce;
	}
	
	public float getSpringForce() {
		return springForce;
	}
	
	public void setSpringForce(float springForce) {
		if (springForce < 0) {
			throw new IllegalArgumentException("The spring force must be greater than or equal to 0");
		}
		this.springForce = springForce;
	}
	
	public float getClusterForce() {
		return clusterForce;
	}
	
	public void setClusterForce(float clusterForce) {
		if (clusterForce < 0) {
			throw new IllegalArgumentException("The cluster force must be greater than or equal to 0");
		}
		this.clusterForce = clusterForce;
	}
	
	public float getMaxForceFactor() {
		return maxForceFactor;
	}
	
	public void setMaxForceFactor(float maxForceFactor) {
		if (maxForceFactor <= 0) {
			throw new IllegalArgumentException("The max force factor must be greater than 0");
		}
		this.maxForceFactor = maxForceFactor;
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
	
	public Variant getVariant() {
		return variant;
	}
	
	public void setVariant(Variant variant) {
		this.variant = variant;
		switch (variant) {
			case SPRING_EMBEDDER:
				variantImplementation = new SpringEnbedderForceCalculator();
				break;
			case FRUCHTERMAN_REINGOLD:
				variantImplementation = new FruchtermanReingoldForceCalculator();
				break;
			default:
				throw new IllegalArgumentException("Unsupported variant type: " + variant);
		}
	}
	
	//************************************************************************
	//*** implementations of variants
	//************************************************************************
	
	private class SpringEnbedderForceCalculator implements VariantForceCalculator<T> {
		
		@Override
		public Vector2D resultingDisplacementVector(T field) {
			Vector2D resultingDisplacementVector = new Vector2D();
			
			// repulsive force between not connected nodes
			for (T other : fields) {
				if (field != other && !graph.isFieldsConnected(field, other)) {
					resultingDisplacementVector = resultingDisplacementVector.add(repulsiveForceVector(field, other));
				}
			}
			
			// attractive force between connected nodes
			for (T connected : connectedFields.get(field)) {
				resultingDisplacementVector = resultingDisplacementVector.add(attractiveSpringForceVector(field, connected));
			}
			
			// attractive force between nodes in a cluster
			for (T clusterField : connectedClusters.get(field)) {
				if (field != clusterField) {
					resultingDisplacementVector = resultingDisplacementVector.add(attractiveClusterForceVector(field, clusterField));
				}
			}
			
			return resultingDisplacementVector;
		}
		
		private Vector2D repulsiveForceVector(T field1, T field2) {
			double forceFactor = repulsiveForce / field1.pos().distance(field2.pos());
			forceFactor = truncateForceFactor(forceFactor);
			return field2.pos().vectorTo(field1.pos()).normalize().mult(forceFactor);
		}
		
		private Vector2D attractiveSpringForceVector(T field1, T field2) {
			double forceFactor = springForce * Math.log10(field1.pos().distance(field2.pos()) / idealSpringLength);
			forceFactor = truncateForceFactor(forceFactor);
			return field1.pos().vectorTo(field2.pos()).normalize().mult(forceFactor);
		}
		
		private Vector2D attractiveClusterForceVector(T field1, T field2) {
			double forceFactor = clusterForce * Math.log10(field1.pos().distance(field2.pos()) / idealClusterDistance);
			forceFactor = truncateForceFactor(forceFactor);
			return field1.pos().vectorTo(field2.pos()).normalize().mult(forceFactor);
		}
	}
	
	private class FruchtermanReingoldForceCalculator implements VariantForceCalculator<T> {
		
		@Override
		public Vector2D resultingDisplacementVector(T field) {
			Vector2D resultingDisplacementVector = new Vector2D();
			
			// repulsive force between all nodes
			for (T other : fields) {
				if (field != other) {
					resultingDisplacementVector = resultingDisplacementVector.add(repulsiveForceVector(field, other));
				}
			}
			
			// attractive force between connected nodes
			for (T connected : connectedFields.get(field)) {
				resultingDisplacementVector = resultingDisplacementVector.add(attractiveSpringForceVector(field, connected));
			}
			
			// attractive force between nodes in a cluster
			for (T clusterField : connectedClusters.get(field)) {
				if (field != clusterField) {
					resultingDisplacementVector = resultingDisplacementVector.add(attractiveClusterForceVector(field, clusterField));
				}
			}
			
			return resultingDisplacementVector;
		}
		
		private Vector2D repulsiveForceVector(T field1, T field2) {
			double forceFactor = repulsiveForce * idealNonAdjacentDistance * idealNonAdjacentDistance / field1.pos().distance(field2.pos());
			forceFactor = truncateForceFactor(forceFactor);
			return field2.pos().vectorTo(field1.pos()).normalize().mult(forceFactor);
		}
		
		private Vector2D attractiveSpringForceVector(T field1, T field2) {
			double forceFactor = springForce * field1.pos().distance2(field2.pos()) / idealSpringLength;
			forceFactor = truncateForceFactor(forceFactor);
			return field1.pos().vectorTo(field2.pos()).normalize().mult(forceFactor);
		}
		
		private Vector2D attractiveClusterForceVector(T field1, T field2) {
			double forceFactor = clusterForce * field1.pos().distance2(field2.pos()) / idealClusterDistance;
			forceFactor = truncateForceFactor(forceFactor);
			return field1.pos().vectorTo(field2.pos()).normalize().mult(forceFactor);
		}
	}
	
	private double truncateForceFactor(double forceFactor) {
		if (Math.abs(forceFactor) > maxForceFactor || forceFactor == Float.NaN) {
			if (forceFactor > maxForceFactor) {
				forceFactor = maxForceFactor;
			}
			else if (forceFactor < -maxForceFactor) {
				forceFactor = -maxForceFactor;
			}
		}
		return forceFactor;
	}
}
