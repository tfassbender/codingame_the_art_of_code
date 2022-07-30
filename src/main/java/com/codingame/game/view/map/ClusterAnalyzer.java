package com.codingame.game.view.map;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.codingame.game.util.Vector2D;

/**
 * Cluster analysis of points using k-means or x-means.
 */
public class ClusterAnalyzer {
	
	private ClusterAnalyzer() {}
	
	public static <T extends Positioned<?>> List<Cluster<T>> getClusters(List<T> allPositions, int minClusters, int maxClusters, double minDistBetweenCentroids) {
		List<Cluster<T>> clusters = getClusters(allPositions, minClusters);
		boolean changing = true;
		while (clusters.size() < maxClusters && changing) {
			changing = false;
			for (int i = 0; i < clusters.size() && clusters.size() < maxClusters; i++) {
				Cluster<T> cluster = clusters.get(i);
				if (cluster.entries.size() >= 2) {
					List<Cluster<T>> splitedCluster = getClusters(cluster.entries, 2);
					if (splitedCluster.get(0).centroid.distance(splitedCluster.get(1).centroid) > minDistBetweenCentroids) {
						//found new clusters
						clusters.remove(i);
						clusters.add(splitedCluster.get(0));
						clusters.add(splitedCluster.get(1));
						i--;
						changing = true;
					}
				}
			}
		}
		return clusters;
	}
	
	public static <T extends Positioned<?>> List<Cluster<T>> getClusters(List<T> allPositions, int numClusters) {
		List<Cluster<T>> clusters = new ArrayList<Cluster<T>>(numClusters);
		//initialize
		if (allPositions.size() < numClusters) {
			throw new IllegalArgumentException("Not enough units for the number of clusters");
		}
		for (int i = 0; i < numClusters; i++) {
			Cluster<T> cluster = new Cluster<T>();
			cluster.centroid = allPositions.get(i).pos();
			clusters.add(cluster);
		}
		boolean changing = true;
		List<Vector2D> startCentroids = new ArrayList<Vector2D>();
		for (int j = 0; j < 100 && changing; j++) {
			startCentroids.clear();
			for (int i = 0; i < clusters.size(); i++) {
				startCentroids.add(clusters.get(i).centroid);
				clusters.get(i).clear();
			}
			//find the nearest centroid for all T
			for (T t : allPositions) {
				Optional<Cluster<T>> nearest = clusters.stream().sorted((c1, c2) -> Double.compare(t.pos().distance(c1.centroid), t.pos().distance(c2.centroid))).findFirst();
				if (nearest.isPresent()) {
					nearest.get().entries.add(t);
				}
				else {
					throw new IllegalArgumentException("No nearest cluster found");
				}
			}
			//calculate new centroid as middle of all T in the cluster
			for (Cluster<T> cluster : clusters) {
				Vector2D newCentroid = new Vector2D(0, 0);
				for (T t : cluster.entries) {
					newCentroid.x += t.pos().x;
					newCentroid.y += t.pos().y;
				}
				newCentroid.x /= cluster.entries.size();
				newCentroid.y /= cluster.entries.size();
				cluster.centroid = newCentroid;
			}
			changing = false;
			for (int i = 0; i < clusters.size(); i++) {
				changing |= !clusters.get(i).centroid.equals(startCentroids.get(i));
			}
		}
		return clusters;
	}
}
