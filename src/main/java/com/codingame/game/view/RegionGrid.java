package com.codingame.game.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.codingame.game.core.Field;
import com.codingame.game.core.Region;
import com.codingame.game.util.DistTool;
import com.codingame.game.util.Pair;
import com.codingame.game.util.Vector2D;
import com.codingame.gameengine.module.entities.GraphicEntityModule;
import com.codingame.gameengine.module.entities.Polygon;

/**
 * Some mixture of Voronoi diagram based on regions and drawing logic for Codingame.
 */
public class RegionGrid {

	/**
	 * Container holding his own index position (xi, yi) in the grid and the draw
	 * coordinate (pos).
	 */
	class GridPoint implements Comparable<GridPoint> {

		Region nearestRegion;
		Vector2D pos;
		int xi, yi;

		public GridPoint(int x, int y, int xi, int yi) {
			pos = new Vector2D(x, y);
			this.xi = xi;
			this.yi = yi;
		}

		public void findNearestRegion(Set<Region> regions, Map<Field, Vector2D> fieldPositions, int norm) {
			// estimate distances for each region
			Collection<Pair<Region, Double>> regionDists = regions.stream()
					.map(r -> Pair.of(r, r.distToPoint(pos, fieldPositions, norm))).collect(Collectors.toSet());

			// get nearest region
			nearestRegion = new DistTool<Region>(regionDists).getMinimumKey();
		}

		public boolean isRegionBoundaryPoint(GridPoint[][] grid) {
			// on map border? -> is boundary point
			if (isOnMapBorder(grid)) {
				return true;
			}

			// go through all neighbors
			for (int i = -1; i <= 1; i += 2) {
				for (int j = -1; j <= 1; j += 2) {
					int xOther = xi + j;
					int yOther = yi + i;

					// neighbor belongs to different region? -> is boundary point
					if (!grid[yOther][xOther].nearestRegion.equals(nearestRegion))
						return true;
				}
			}

			// no reason fulfilled to be a boundary point
			return false;
		}

		private boolean isOnMapBorder(GridPoint[][] grid) {
			return xi == 0 || yi == 0 || yi == grid.length - 1 || xi == grid[0].length - 1;
		}

		@Override
		public int compareTo(GridPoint o) {
			if (xi == o.xi) {
				return Integer.compare(yi, o.yi);
			} else {
				return Integer.compare(xi, o.xi);
			}
		}
	}

	// region/field parameters
	private Set<Region> regions;
	private Map<Field, Vector2D> fieldPositions;

	// for view placement and calculations
	private int width = 100;
	private int height = 100;
	private int x = 0;
	private int y = 0;

	// shape parameters
	private int resolution = 1; // higher number -> faster, but less accurate
	private int distNorm = 2; // influences the shape of the regions

	// grid points (set for streams, array for indexing operations)
	private Set<GridPoint> gridSet;
	private GridPoint[][] grid;

	public RegionGrid(Set<Region> regions, Map<Field, Vector2D> fieldPositions) {
		this.regions = regions;
		this.fieldPositions = fieldPositions;
	}

	// **********************************************************************
	// *** setters
	// **********************************************************************

	public void setDistNorm(int norm) {
		this.distNorm = norm;
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

	// **********************************************************************
	// *** the actual usage
	// **********************************************************************

	public Map<Region, Polygon> createRegionPolygons(GraphicEntityModule graphicEntityModule) {
		// create grid
		initGrid();

		// find nearest region for each grid point
		estimateNearestRegion();

		// find region boundaries
		Map<Region, Set<GridPoint>> boundaries = estimateRegionBoundaries();

		// get only the boundaries of the regions (sorted)
		Map<Region, List<GridPoint>> polygonPoints = toPolygonPoint(boundaries);
		
		// try to keep only the vertices of the polygon
		simplifyBoundaries(polygonPoints);

		// create graphics (polygons) from boundaries
		Map<Region, Polygon> regionPolygons = createPolygons(polygonPoints, graphicEntityModule);

		return regionPolygons;
	}

	// **********************************************************************
	// *** private methods
	// **********************************************************************

	/**
	 *  Sometimes needed to avoid problems with high amount of graphical data.
	 *  Still some imperfections, because of a lacking end point to start point (circular) check.
	 */
	private void simplifyBoundaries(Map<Region, List<GridPoint>> polygonPoints) {
		boolean useAggressiveMethod = true;
		// step size is 2 because of grid structure (only if we don't use other methods before=
		//	x 0 0 
		//  x x 0
		//  0 x 0
		int stepSize = useAggressiveMethod ? 1 : 2;
		
		if (useAggressiveMethod) {
			simplifiyBoundsAggressively(polygonPoints);
		}
		
		for (Region region : regions) {
			List<GridPoint> points = polygonPoints.get(region);
			
			for (int i = points.size()-3*stepSize; i >= 0; i--) {
				Vector2D p1 = points.get(i).pos;
				Vector2D p2 = points.get(i+stepSize).pos;
				Vector2D p3 = points.get(i+2*stepSize).pos;
				
				// remove p2 if p2 is on the line p1->p3
				
				if (p1.vectorTo(p3).setLength(1).isCloseTo(p2.vectorTo(p3).setLength(1)))
					points.remove(i+stepSize);
			}
		}
	}
	
	private void simplifiyBoundsAggressively(Map<Region, List<GridPoint>> polygonPoints) {
		// another algorithm, effective but less smooth looking
		for (Region region : regions) {
			List<GridPoint> points = polygonPoints.get(region);
			
			for (int i = points.size()-3; i >= 0; i--) {
				GridPoint p1 = points.get(i);
				GridPoint p2 = points.get(i+1);
				GridPoint p3 = points.get(i+2);
				
				int dx1 = p3.xi - p2.xi;
				int dy1 = p3.yi - p2.yi;
				int dx2 = p2.xi - p1.xi;
				int dy2 = p2.yi - p1.yi;
				// remove p2 if p2 is on the line p1->p3
				
				if (Math.abs(dx1+dy1) == Math.abs(dx2 + dy2)) {
					points.remove(i+1);
				}
			}
		}
	}

	private Map<Region, Polygon> createPolygons(Map<Region, List<GridPoint>> polygonPoints,
			GraphicEntityModule graphicEntityModule) {
		Map<Region, Polygon> polygons = new HashMap<Region, Polygon>();

		for (Region region : regions) {
			// create polygon
			Polygon polygon = graphicEntityModule.createPolygon();

			// add all points (the order is somewhat important)
			for (GridPoint point : polygonPoints.get(region)) {
				polygon.addPoint((int) point.pos.x, (int) point.pos.y);
			}

			polygons.put(region, polygon);
		}

		return polygons;
	}

	private Map<Region, List<GridPoint>> toPolygonPoint(Map<Region, Set<GridPoint>> boundaries) {
		Map<Region, List<GridPoint>> polygonPoints = new HashMap<Region, List<GridPoint>>();

		for (Region region : regions) {
			List<GridPoint> orderedBoundaries = new ArrayList<GridPoint>();
			Set<GridPoint> inscope = new HashSet<GridPoint>(boundaries.get(region));
			
			// start the boundary sort on the upper left point
			GridPoint currentPoint = inscope.stream().sorted().findFirst().get();

			
			// update visited + boundaries
			orderedBoundaries.add(currentPoint);
			inscope.remove(currentPoint);

			// visit all boundary points
			while (inscope.size() > 0) {
				// find closest and not yet saved point
				final Vector2D pos = currentPoint.pos;
				Collection<Pair<GridPoint, Double>> pointDists = inscope.stream()
						.map(p -> Pair.of(p, p.pos.distance(pos))).collect(Collectors.toSet());
				GridPoint nextPoint = new DistTool<GridPoint>(pointDists).getMinimumKey();

				// stop the border once we would jump through the map (happens if we skip one point earlier)
				if (currentPoint.pos.distance(nextPoint.pos) >= 4 * resolution)
					break;

				// update visited + boundaries
				inscope.remove(nextPoint);
				orderedBoundaries.add(nextPoint);

				// continue the border from the next point
				currentPoint = nextPoint;
			}

			polygonPoints.put(region, orderedBoundaries);
		}

		return polygonPoints;
	}

	private Map<Region, Set<GridPoint>> estimateRegionBoundaries() {
		Map<Region, Set<GridPoint>> boundaries = new HashMap<Region, Set<GridPoint>>();

		for (Region region : regions) {
			// filter: by region
			Set<GridPoint> regionPoints = gridSet.stream() //
					.filter(p -> p.nearestRegion.equals(region)) //
					.collect(Collectors.toSet());
			
			// filter: is boundary point
			Set<GridPoint> regionBoundaries = regionPoints.stream() //
					.filter(p -> p.isRegionBoundaryPoint(grid)) // 
					.collect(Collectors.toSet());

			boundaries.put(region, regionBoundaries);
		}

		return boundaries;
	}

	private void estimateNearestRegion() {
		gridSet.stream().forEach(p -> p.findNearestRegion(regions, fieldPositions, distNorm));
	}

	private void initGrid() {
		// resolution = stepSize in x,y
		int ysteps = height / resolution;
		int xsteps = width / resolution;

		grid = new GridPoint[ysteps][xsteps];
		gridSet = new HashSet<GridPoint>();

		for (int i = 0; i < ysteps; i++) {
			int ys = y + i * resolution;

			for (int j = 0; j < xsteps; j++) {
				int xs = x + j * resolution;

				grid[i][j] = new GridPoint(xs, ys, j, i);
				gridSet.add(grid[i][j]);
			}
		}
	}
}
