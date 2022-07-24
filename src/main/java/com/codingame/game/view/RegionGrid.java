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
	class GridPoint {

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

		// create graphics (polygons) from boundaries
		Map<Region, Polygon> regionPolygons = createPolygons(polygonPoints, graphicEntityModule);

		return regionPolygons;
	}

	// **********************************************************************
	// *** private methods
	// **********************************************************************

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
			
			// start the boundary sort on any point of the boundary
			GridPoint currentPoint = inscope.stream().findFirst().get();

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
