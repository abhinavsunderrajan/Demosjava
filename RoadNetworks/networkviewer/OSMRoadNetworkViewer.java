package networkviewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Polygon;

import networkmodel.GeoHash;
import networkmodel.OSMRoadNetworkModel;
import networkmodel.PlanningArea;
import networkmodel.QuadTree;
import networkmodel.Road;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;

/**
 * Visualizing road network
 * 
 * @author abhinav.sunderrajan
 *
 */
public class OSMRoadNetworkViewer extends RoadNetworkVisualizer {

    private BufferedImage background = null;
    private int backgroundXOffset = -1;
    private int backgroundYOffset = -1;
    private List<Coordinate> dataPoints;
    private static final boolean DRAW_GEOHASH = false;
    private static final boolean DRAW_QUADTREE = false;
    private static final boolean LOAD_PLANNING_AREAS = false;
    private static final int CITY_ID = 6;
    private static final String MAP_VERSION = "2019-8";
    private static final String DIR_ROAD_FILES = "/Users/abhinav.sunderrajan/Desktop/road-speed-profiling/abhinav/road_network_files/";
    private QuadTree quadTree;
    private static Map<String, PlanningArea> planningAreas;

    /**
     * Create the road network viewer.
     * 
     * @param title the title for the viewer
     * @param model the road-network model.
     */
    public OSMRoadNetworkViewer(String title, RoadNetworkModel model) {
	super(title, model);
	dataPoints = new ArrayList<Coordinate>();
    }

    @Override
    public void updateView() {
	if (model.getAllNodes().isEmpty())
	    return;

	image = new BufferedImage(panel.getWidth(), panel.getHeight(), BufferedImage.TYPE_INT_RGB);

	Graphics g = image.getGraphics();
	Graphics2D g2 = (Graphics2D) g;
	g2.setColor(Color.LIGHT_GRAY);
	g2.fillRect(0, 0, panel.getWidth(), panel.getHeight());

	synchronized (lock) {

	    if (background != null)
		g2.drawImage(background, backgroundXOffset, backgroundYOffset, null);

	    visibleRoads.stream().forEach(road -> {
		String roadType = road.getRoadType();
		g2.setColor(Color.BLACK);

		if (roadType.contains("trunk")) {
		    g2.setStroke(new BasicStroke(4));
		} else if (roadType.contains("primary")) {
		    g2.setStroke(new BasicStroke(3));
		} else if (roadType.contains("secondary")) {
		    g2.setStroke(new BasicStroke(2));
		} else if (roadType.contains("motorway")) {
		    g2.setStroke(new BasicStroke(4));
		} else {
		    g2.setColor(Color.DARK_GRAY);
		    g2.setStroke(new BasicStroke(1));
		}

		drawRoad(road, g2);
	    });

	}

	if (nearestRoad != null) {
	    g2.setColor(Color.ORANGE);
	    drawRoad(nearestRoad, g2);
	}

	// draw the geohash
	if (DRAW_GEOHASH) {
	    for (GeoHash leaf : model.getGeoHashMap().values()) {
		g2.setColor(Color.GREEN);
		g2.setStroke(new BasicStroke(4));
		int[] xy0 = lonLatToVizXY(leaf.getBounds().getMinX(), leaf.getBounds().getMaxY());
		int[] xy1 = lonLatToVizXY(leaf.getBounds().getMaxX(), leaf.getBounds().getMinY());
		g2.drawRect(xy0[0], xy0[1], (xy1[0] - xy0[0]), (xy1[1] - xy0[1]));
	    }
	}

	// drawing quadtree
	if (DRAW_QUADTREE) {
	    for (QuadTree leaf : quadTree.getAllLeaves()) {
		g2.setColor(Color.GREEN);
		g2.setStroke(new BasicStroke(4));
		int[] xy0 = lonLatToVizXY(leaf.getBounds().getMinX(), leaf.getBounds().getMaxY());
		int[] xy1 = lonLatToVizXY(leaf.getBounds().getMaxX(), leaf.getBounds().getMinY());
		g2.drawRect(xy0[0], xy0[1], (xy1[0] - xy0[0]), (xy1[1] - xy0[1]));
	    }

	}

	// draw the planning areas
	if (LOAD_PLANNING_AREAS) {
	    for (PlanningArea pa : planningAreas.values()) {
		Polygon polygon = pa.getPolygon();
		g2.setColor(Color.GREEN);
		g2.setStroke(new BasicStroke(5));
		drawPolygon(polygon, g2);
	    }

	}

	for (Coordinate position : dataPoints) {
	    g2.setColor(Color.ORANGE);
	    int xy[] = lonLatToVizXY(position.x, position.y);
	    Ellipse2D.Double circle = new Ellipse2D.Double(xy[0], xy[1], 15, 15);
	    g2.fill(circle);

	}

	synchronized (lock) {
	    for (Road link : selectedRoads) {
		g.setColor(Color.RED);
		g2.setStroke(new BasicStroke(4));
		drawRoad(link, g2);
	    }
	}

	synchronized (lock) {
	    for (RoadNode node : selectedNodes) {
		g.setColor(Color.MAGENTA);
		int xy[] = lonLatToVizXY(node.getX(), node.getY());
		Ellipse2D.Double circle = new Ellipse2D.Double(xy[0], xy[1], 10, 10);
		g2.fill(circle);
	    }
	}
    }

    public static void main(String[] args) throws Exception {

	OSMRoadNetworkModel roadNetworkModel = new OSMRoadNetworkModel(
		DIR_ROAD_FILES + MAP_VERSION + "/normalized/" + "roads_" + CITY_ID + "_" + MAP_VERSION + ".txt",
		DIR_ROAD_FILES + MAP_VERSION + "/unnormalized/" + "nodes_" + CITY_ID + "_" + MAP_VERSION + ".txt",
		CITY_ID, 5, LOAD_PLANNING_AREAS, false);

	// fire up the visualization
	OSMRoadNetworkViewer viewer = new OSMRoadNetworkViewer("Open-street map " + MAP_VERSION, roadNetworkModel);

	// load the planning areas
	if (roadNetworkModel.isLoadPlanningAreas()) {
	    planningAreas = roadNetworkModel.getPlanningAreas();
	    for (String name : planningAreas.keySet())
		System.out.println(name);
	}

	long highlightRoads[] = {};

	for (long roadId : highlightRoads) {
	    Road road = viewer.model.getAllRoadsMap().get(roadId);
	    if (road == null)
		System.err.println("Road: " + roadId + " does not exist");
	    else
		viewer.selectedRoads.add(road);
	}

	for (Road road : viewer.selectedRoads)
	    System.out.println(road + " " + road.getRoadType() + " " + road.getLength() + " "
		    + road.getBeginNode().getY() + "," + road.getBeginNode().getX() + "," + road.getEndNode().getY()
		    + "," + road.getEndNode().getX());

	Long[] highlightNodes = {};

//	JSONObject json = new JSONObject(
//		"{\"375778681\":\"secondary\",\"242941694\":\"secondary\",\"244317326\":\"primary\",\"242941570\":\"secondary\",\"206101425\":\"tertiary\",\"206101421\":\"secondary\",\"368902719\":\"residential\",\"243208139\":\"motorway_link\",\"368902639\":\"tertiary\",\"206261534\":\"secondary\",\"243415158\":\"tertiary\",\"242941719\":\"secondary\",\"206098237\":\"primary\",\"373096980\":\"residential\",\"375778348\":\"primary\",\"242941536\":\"tertiary\",\"373096628\":\"tertiary\",\"395048575\":\"residential\",\"380485767\":\"secondary\",\"242941481\":\"secondary\",\"241595342\":\"primary\",\"308938149\":\"primary\",\"245567127\":\"residential\",\"380485280\":\"tertiary\",\"242941626\":\"tertiary\"}");
//	highlightNodes = json.keySet().stream().map(id -> Long.parseLong(id)).collect(Collectors.toList())
//		.toArray(new Long[0]);

	for (long nodeId : highlightNodes) {
	    RoadNode node = viewer.model.getAllNodes().get(nodeId);
	    if (node == null) {
		System.err.println("Node: " + nodeId + " does not exist");
		continue;
	    }

	    viewer.selectedNodes.add(node);
	}

    }

}
