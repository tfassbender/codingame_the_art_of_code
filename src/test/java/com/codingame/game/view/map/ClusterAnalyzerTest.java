package com.codingame.game.view.map;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.codingame.game.util.Vector2D;

public class ClusterAnalyzerTest {
	
	private final List<Vector2D> clusterCenters = Arrays.asList(new Vector2D(20, 20), new Vector2D(40, 10), new Vector2D(10, 50), new Vector2D(-20, -20));
	
	@Test
	public void test_k_means_two_clusters() {
		List<PositionedObject> positions = createClusters(2, 10);
		List<Cluster<PositionedObject>> clusters = ClusterAnalyzer.getClusters(positions, 2);
		
		assertEquals(2, clusters.size());
		assertEquals(10, clusters.get(0).entries.size());
		assertEquals(10, clusters.get(1).entries.size());
	}
	
	@Test
	public void test_k_means_three_clusters() {
		List<PositionedObject> positions = createClusters(3, 10);
		List<Cluster<PositionedObject>> clusters = ClusterAnalyzer.getClusters(positions, 3);
		
		assertEquals(3, clusters.size());
		assertEquals(10, clusters.get(0).entries.size());
		assertEquals(10, clusters.get(1).entries.size());
		assertEquals(10, clusters.get(2).entries.size());
	}
	
	@Test
	public void test_k_means_four_clusters() {
		List<PositionedObject> positions = createClusters(4, 10);
		List<Cluster<PositionedObject>> clusters = ClusterAnalyzer.getClusters(positions, 4);
		
		assertEquals(4, clusters.size());
		assertEquals(10, clusters.get(0).entries.size());
		assertEquals(10, clusters.get(1).entries.size());
		assertEquals(10, clusters.get(2).entries.size());
		assertEquals(10, clusters.get(3).entries.size());
	}
	
	@Test
	public void test_x_means_four_clusters() {
		List<PositionedObject> positions = createClusters(4, 10);
		List<Cluster<PositionedObject>> clusters = ClusterAnalyzer.getClusters(positions, 2, 5, 10);
		
		assertEquals(4, clusters.size());
		assertEquals(10, clusters.get(0).entries.size());
		assertEquals(10, clusters.get(1).entries.size());
		assertEquals(10, clusters.get(2).entries.size());
		assertEquals(10, clusters.get(3).entries.size());
	}
	
	public List<PositionedObject> createClusters(int clusters, int pointsPerCluster) {
		List<PositionedObject> positions = new ArrayList<>(clusters * pointsPerCluster);
		
		for (int i = 0; i < clusters; i++) {
			for (int j = 0; j < pointsPerCluster; j++) {
				positions.add(new PositionedObject(clusterCenters.get(i).add(new Vector2D(Math.random() * 5, Math.random() * 5))));
			}
		}
		
		return positions;
	}
	
	private class PositionedObject implements Positioned<Object> {
		
		private Vector2D position;
		
		public PositionedObject(Vector2D position) {
			this.position = position;
		}
		
		@Override
		public Vector2D pos() {
			return position;
		}
		
		@Override
		public void setPosition(Vector2D position) {
			this.position = position;
		}
	}
}
