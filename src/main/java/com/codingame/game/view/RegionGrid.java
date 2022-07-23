package com.codingame.game.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.Region;
import com.codingame.game.util.DistTool;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Polygon;

public class RegionGrid {

	class GridPoint {
		Region nearestRegion;
		Vector2D pos;
		int xi,yi;
		
		public GridPoint(int x, int y, int xi, int yi) {
			pos = new Vector2D(x, y);
			this.xi = xi;
			this.yi = yi;
		}

		public void findNearestRegion(Set<Region> regions, Map<Field, Vector2D> fieldPositions, int norm) {
			Collection<Pair<Region, Double>> regionDists = regions.stream().map(r -> Pair.of(r, r.fields.stream().mapToDouble(f -> fieldPositions.get(f).sub(pos).length(norm)).min().getAsDouble())).collect(Collectors.toSet());
			nearestRegion = new DistTool<Region>(regionDists).getMinimumKey();
		}

		public boolean isBoundaryPoint(GridPoint[][] grid) {
			// on map border?
			if (xi <= 0 || yi <= 0 || yi >= grid.length-1 || xi >= grid[0].length-1) {
				return true;
			}			
			
			for (int i = -1; i <= 1; i+=2) {
				for (int j = -1; j <= 1; j+=2) {
					int xOther = xi+j;
					int yOther = yi+i;
					
					if (!grid[yOther][xOther].nearestRegion.equals(nearestRegion))
						return true;
				}
			}
			
			return false;
		}
	}
	
	Set<Region> regions;
	Map<Field, Vector2D> fieldPositions;
	
	int width = 100;
	int height = 100;
	int x = 0;
	int y = 0;
	int resolution = 1;
	int distNorm = 2;
	
	Set<GridPoint> gridSet;
	GridPoint[][] grid;
	int[] xs;
	int[] ys;
	
	public RegionGrid(Set<Region> regions, Map<Field, Vector2D> fieldPositions) {
		this.regions = regions;
		this.fieldPositions = fieldPositions;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public void setX(int x) {
		this.x = x;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setResolution(int resolution) {
		this.resolution = resolution;
	}

	public Map<Region, Polygon> createRegionPolygons(GraphicEntityModule graphicEntityModule) {
		// create grid
		initGrid();
		
		// find nearest region for each grid point
		estimateNearestRegion();
		
		// find region boundaries
		Map<Region, Set<GridPoint>> boundaries = estimateRegionBoundaries();
		
		// simplify region shape/boundaries (as least points as possible)
		Map<Region, List<GridPoint>> polygonPoints = toPolygonPoint(boundaries);
		
		Map<Region, Polygon> regionPolys = createPolygons(polygonPoints, graphicEntityModule);
		
		return regionPolys;
	}

	private Map<Region, Polygon> createPolygons(Map<Region, List<GridPoint>> polygonPoints, GraphicEntityModule graphicEntityModule) {
		Map<Region, Polygon> polys = new HashMap<Region, Polygon>();
		
		for (Region region : regions) {
			Polygon poly = graphicEntityModule.createPolygon();
			
			for (GridPoint point : polygonPoints.get(region)) {
				poly.addPoint((int) point.pos.x, (int) point.pos.y);
			}
			
//			poly.addPoint((int) polygonPoints.get(region).get(0).pos.x, (int) polygonPoints.get(region).get(0).pos.x);
			
			polys.put(region, poly);
		}
		
		return polys;
	}

	private Map<Region, List<GridPoint>> toPolygonPoint(Map<Region, Set<GridPoint>> boundaries) {
		Map<Region, List<GridPoint>> polypoints = new HashMap<Region, List<GridPoint>>();

		for (Region region : regions) {
			List<GridPoint> orderedBoundaries = new ArrayList<GridPoint>();
			Set<GridPoint> inscope = new HashSet<GridPoint>(boundaries.get(region));
			GridPoint currentPoint = inscope.stream().findFirst().get();
			
			orderedBoundaries.add(currentPoint);
			inscope.remove(currentPoint);
			
			while(inscope.size() > 0) {
				final Vector2D pos = currentPoint.pos;
				Collection<Pair<GridPoint, Double>> pointDists = inscope.stream().map(p -> Pair.of(p, p.pos.distance(pos))).collect(Collectors.toSet());
				GridPoint nextPoint = new DistTool<GridPoint>(pointDists).getMinimumKey();

				// stop the border once we have issues like that (happens we we skip one point)
				if (currentPoint.pos.distance(nextPoint.pos) >= 4*resolution)
					break;
				
				inscope.remove(nextPoint);
				orderedBoundaries.add(nextPoint);
				
				currentPoint = nextPoint;
			}
			
			polypoints.put(region, orderedBoundaries);
		}
		
		return polypoints;
	}

	private Map<Region, Set<GridPoint>> estimateRegionBoundaries() {
		Map<Region, Set<GridPoint>> boundaries = new HashMap<Region, Set<GridPoint>>();

		for (Region region : regions) {
			Set<GridPoint> regionPoints = gridSet.stream().filter(p -> p.nearestRegion.equals(region)).collect(Collectors.toSet());
			Set<GridPoint> regionBoundaries = regionPoints.stream().filter(p -> p.isBoundaryPoint(grid)).collect(Collectors.toSet());
			
			boundaries.put(region, regionBoundaries);
		}
		
		return boundaries;
	}

	private void estimateNearestRegion() {
		gridSet.stream().forEach(p -> p.findNearestRegion(regions, fieldPositions, distNorm));
	}

	private void initGrid() {
		int ysteps = height/resolution;
		int xsteps = width/resolution;
		
		xs = new int[xsteps];
		ys = new int[ysteps];
		grid = new GridPoint[ysteps][xsteps];
		gridSet = new HashSet<GridPoint>();
		
		for (int i = 0; i < xsteps; i++) {
			xs[i] = x + i * resolution;
		}
		
		for (int i = 0; i < ysteps; i++) {
			ys[i] = y + i * resolution;
		}
		
		for (int i = 0; i < ysteps; i++) {
			for (int j = 0; j < xsteps; j++) {
				grid[i][j] = new GridPoint(xs[j], ys[i], j, i);
				gridSet.add(grid[i][j]);
			}
		}
	}

	public void setDistNorm(int norm) {
		this.distNorm = norm;
	}
}
