package routing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.json.JSONArray;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;

import networkmodel.OSMRoadNetworkModel;
import networkmodel.Road;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;
import networkutils.GeoFunctions;
import networkviewer.OSMRoadNetworkViewer;
import networkviewer.RoadNetworkVisualizer;

/**
 * Input the road network model and get the route.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class Djikstra extends RoutingAlgoAbstract {

    /**
     * Initialize with the road network model for normal routing using Dijkstra.
     * 
     * @param rnwModel
     * @throws ParseException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public Djikstra(RoadNetworkModel rnwModel) throws FileNotFoundException, IOException {
	super(rnwModel);

    }

    @Override
    public void djikstra(RoadNode srcNode, RoadNode destNode) {

	if (srcNode == destNode) {
	    sameODNode = true;
	    return;
	}

	if (!srcNode.isBeginOrEnd()) {
	    for (Road road : srcNode.getInRoads()) {

		double distances[] = computeDistanceToBeginAndEndNodes(road, srcNode);
		double distEnd = distances[1];
		srcNode = road.getEndNode();
		distanceDelta = distanceDelta + distEnd;
		break;
	    }
	}

	if (!destNode.isBeginOrEnd()) {
	    for (Road road : destNode.getOutRoads()) {
		double distances[] = computeDistanceToBeginAndEndNodes(road, destNode);
		double distEnd = distances[1];

		destNode = road.getBeginNode();
		distanceDelta = distanceDelta + distEnd;
		break;
	    }
	}

	if (srcNode == destNode) {
	    sameODNode = true;
	    return;
	}

	all.get(srcNode).minDistanceForward = 0;
	all.get(srcNode).setForward(true);
	unseenForward.add(all.get(srcNode));
	visited.add(all.get(srcNode));

	while (unseenForward.size() > 0) {
	    Vertex beginVertex = unseenForward.pollFirst();
	    if (beginVertex.minDistanceForward == Double.MAX_VALUE)
		return;

	    if (beginVertex.node.getOutRoads() != null) {

		for (Road road : beginVertex.node.getOutRoads()) {

		    Vertex nextVertex = all.get(road.getEndNode());
		    if (seenForward.contains(nextVertex))
			continue;

		    // This part is important since you always want the regular
		    // Dijkstra algorithm
		    nextVertex.setForward(true);

		    if (nextVertex.minDistanceForward == Double.POSITIVE_INFINITY) {
			nextVertex.minDistanceForward = road.getRoadWeight(criteria) + beginVertex.minDistanceForward;
			nextVertex.previousF = beginVertex.node;
			nextVertex.connectingRoad = road;
			visited.add(nextVertex);
			unseenForward.add(nextVertex);
		    } else {
			double temp = beginVertex.minDistanceForward + road.getRoadWeight(criteria);
			if (temp < nextVertex.minDistanceForward) {
			    if (unseenForward.contains(nextVertex))
				unseenForward.remove(nextVertex);

			    nextVertex.minDistanceForward = temp;
			    nextVertex.connectingRoad = road;
			    nextVertex.previousF = beginVertex.node;
			    unseenForward.add(nextVertex);

			}
		    }
		}
	    }

	    seenForward.add(beginVertex);
	    if (beginVertex.node.getNodeId() == destNode.getNodeId())
		break;

	}

	Vertex vp = all.get(destNode);

	while (true) {
	    if (vp.node.getNodeId() == srcNode.getNodeId()) {
		routeNodes.add(vp);
		break;
	    } else {
		routeNodes.add(vp);
		vp = all.get(vp.previousF);
		if (vp == null) {
		    routeNodes.clear();
		    return;
		}
	    }
	}
    }

    @Override
    public ArrayList<Road> getRoute() {
	// ArrayList<Road> path = new ArrayList<Road>();
	if (path.size() == 0) {
	    for (int count = routeNodes.size() - 2; count >= 0; count--) {
		Road link = routeNodes.get(count).connectingRoad;
		if (link == null)
		    continue;
		path.add(link);
	    }
	}
	return path;
    }

    @Override
    public List<RoadNode> getRouteInNodes() {
	// TODO Auto-generated method stub
	return null;
    }

    public static void main(String args[]) throws Exception {

	String mapVersion = "2019-20";
	int cityId = 6;
	Properties properties = new Properties();
	properties.load(new FileInputStream("src/main/resources/config.properties"));

	String dirRoadNetworkFiles = properties.getProperty("road.network.files.dir");

	OSMRoadNetworkModel roadNetworkModel = new OSMRoadNetworkModel(
		dirRoadNetworkFiles + mapVersion + "/normalized/" + "roads_" + cityId + "_" + mapVersion + ".txt",
		dirRoadNetworkFiles + mapVersion + "/unnormalized/" + "nodes_" + cityId + "_" + mapVersion + ".txt",
		cityId, 6, false, false);

	Djikstra rd = new Djikstra(roadNetworkModel);
	long t1 = System.currentTimeMillis();

	RoadNode origin = roadNetworkModel.getNearestNodesSortedByDistance(new Coordinate(103.624666, 1.302979)).get(0);
	RoadNode destination = roadNetworkModel.getNearestNodesSortedByDistance(new Coordinate(103.828235, 1.464160))
		.get(0);

	rd.djikstra(origin, destination);
	System.out.println(
		"Execution time: " + (System.currentTimeMillis() - t1) + " ms visited node size: " + rd.visited.size());

	getRouteDetails(rd.getRoute(), rd.getDistanceDelta(), rd.isSameODNode());
	RoadNetworkVisualizer viewer = new OSMRoadNetworkViewer("Open-streetmap", roadNetworkModel);
	viewer.getSelectedRoads().addAll(rd.getRoute());
	rd.reset();

    }

    /**
     * Computes the distance of a node to and from the begin and end nodes in
     * meters.
     * 
     * @param road
     * @param roadNode
     * @return the array with first element is the distance to the begin node and
     *         the other is the distance to the end node.
     */
    private double[] computeDistanceToBeginAndEndNodes(Road road, RoadNode roadNode) {
	double distbegin = 0;
	List<RoadNode> roadNodes = road.getRoadNodes();
	int j = 0;
	for (int i = 1; i < roadNodes.size(); i++) {
	    RoadNode node = roadNodes.get(i);
	    distbegin += GeoFunctions.haversineDistance(node.getPosition(), roadNodes.get(i - 1).getPosition());
	    if (node.equals(roadNode)) {
		j = i;
		break;
	    }

	}

	double distEnd = 0;
	for (int i = j + 1; i < roadNodes.size(); i++) {
	    RoadNode node = roadNodes.get(i);
	    distEnd += GeoFunctions.haversineDistance(node.getPosition(), roadNodes.get(i - 1).getPosition());
	}
	double distances[] = { distbegin, distEnd };
	return distances;
    }

    private static void getRouteDetails(ArrayList<Road> route, double delta, boolean isSameODNode) {
	JSONObject obj = new JSONObject();
	double distance = 0.0;
	double expectedTime = 1.0;
	double bestCaseTime = 1.0;
	double worstCaseTime = 1.0;
	double medianTime = 1.0;

	JSONArray jsonArray = new JSONArray();
	if (route.size() > 0) {
	    int i = 0;
	    for (Road road : route) {
		distance += road.getLength();
		double meanSpeed = road.getExpectedSpeed();
		double medianSpeed = road.getMedianSpeed();
		double lowerBoundSpeed = road.getLowerBoundSpeed();
		double upperBoundSpeed = road.getUpperBoundSpeed();

		if (i == 0) {
		    expectedTime += (road.getLength() + delta) / meanSpeed;
		    medianTime += (road.getLength() + delta) / medianSpeed;
		    worstCaseTime += (road.getLength() + delta) / lowerBoundSpeed;
		    bestCaseTime += (road.getLength() + delta) / upperBoundSpeed;

		} else {
		    expectedTime += road.getLength() / meanSpeed;
		    medianTime += road.getLength() / medianSpeed;
		    worstCaseTime += road.getLength() / lowerBoundSpeed;
		    bestCaseTime += road.getLength() / upperBoundSpeed;
		}

		jsonArray.put(road.getRoadId());
		i++;
	    }
	} else {
	    // if the OD node are the same i am assuming speed of arrival is
	    // around 5 m/s
	    if (isSameODNode) {
		expectedTime += delta / 5.0;
		medianTime += delta / 5.0;
		worstCaseTime += delta / 5.0;
		bestCaseTime += delta / 5.0;

	    }
	}
	obj.put("sameODNode", isSameODNode);
	obj.put("route", jsonArray);
	obj.put("distance", (distance + delta));
	obj.put("mean_time", expectedTime);
	obj.put("median_time", medianTime);
	obj.put("worst_case_time", worstCaseTime);
	obj.put("best_case_time", bestCaseTime);
	System.out.println(obj);
	;
    }

}
