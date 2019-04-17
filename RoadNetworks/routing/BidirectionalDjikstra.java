package routing;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import networkmodel.OSMRoadNetworkModel;
import networkmodel.Road;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;
import networkutils.GeoFunctions;
import networkviewer.OSMRoadNetworkViewer;
import networkviewer.RoadNetworkVisualizer;

/**
 * Bi-directional Dijkstra routing alternating between forward and backward
 * search.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class BidirectionalDjikstra extends RoutingAlgoAbstract {

	/**
	 * Initialize with the road network model for normal routing using Dijkstra.
	 * 
	 * @param rnwModel
	 * @throws ParseException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public BidirectionalDjikstra(RoadNetworkModel rnwModel) throws FileNotFoundException, IOException {
		super(rnwModel);
	}

	@Override
	public void djikstra(RoadNode srcNode, RoadNode destNode) {
		if (!srcNode.isBeginOrEnd()) {
			for (Road road : srcNode.getInRoads()) {
				double distbegin = GeoFunctions.haversineDistance(road.getBeginNode().getPosition(),
						srcNode.getPosition());
				double distEnd = GeoFunctions.haversineDistance(road.getEndNode().getPosition(), srcNode.getPosition());
				srcNode = distbegin < distEnd ? road.getBeginNode() : road.getEndNode();
				distanceDelta = distbegin < distEnd ? (distanceDelta - distbegin) : (distanceDelta + distEnd);
				break;
			}
		}

		if (!destNode.isBeginOrEnd()) {
			for (Road road : destNode.getOutRoads()) {
				double distbegin = GeoFunctions.haversineDistance(road.getBeginNode().getPosition(),
						destNode.getPosition());
				double distEnd = GeoFunctions.haversineDistance(road.getEndNode().getPosition(),
						destNode.getPosition());
				destNode = distbegin < distEnd ? road.getBeginNode() : road.getEndNode();
				distanceDelta = distbegin < distEnd ? (distanceDelta + distbegin) : (distanceDelta - distEnd);
				break;
			}
		}

		all.get(srcNode).minDistanceForward = 0;
		all.get(srcNode).setForward(true);

		all.get(destNode).minDistanceBackward = 0;
		all.get(destNode).setForward(false);

		unseenForward.add(all.get(srcNode));
		unseenBackward.add(all.get(destNode));

		visited.add(all.get(srcNode));
		visited.add(all.get(destNode));

		boolean routeForward = true;
		boolean foundRoute = false;

		// int graphSize = unseen.size();
		while (unseenForward.size() > 0 && unseenBackward.size() > 0) {

			TreeSet<Vertex> whichSet = routeForward ? unseenForward : unseenBackward;
			Vertex beginVertex = whichSet.pollFirst();

			// Not possible hence null route
			if (beginVertex.getDistance(routeForward) == Double.MAX_VALUE)
				return;

			if (routeForward)
				beginVertex.isProcessedForward = true;
			else
				beginVertex.isProcessedBackward = true;

			// Exit condition, vertex has been processed both in the forward and
			// backward direction.
			if (beginVertex.isProcessedForward && beginVertex.isProcessedBackward) {
				// System.out.println(
				// "Node processed by both queues finished running bidirectional
				// Dijkstra " + visited.size());
				w = beginVertex;
				foundRoute = true;
			}

			visited.add(beginVertex);

			Set<Road> nextRoads = routeForward ? beginVertex.node.getOutRoads() : beginVertex.node.getInRoads();

			if (nextRoads != null) {

				for (Road road : nextRoads) {

					Vertex nextVertex = null;
					if (road.isOneWay()) {
						nextVertex = routeForward ? all.get(road.getEndNode()) : all.get(road.getBeginNode());

					} else {
						if (routeForward) {
							if (road.getEndNode().getNodeId() == beginVertex.node.getNodeId())
								nextVertex = all.get(road.getBeginNode());
							else
								nextVertex = all.get(road.getEndNode());
						} else {
							if (road.getBeginNode().getNodeId() == beginVertex.node.getNodeId())
								nextVertex = all.get(road.getEndNode());
							else
								nextVertex = all.get(road.getBeginNode());
						}

					}

					if (routeForward && seenForward.contains(nextVertex))
						continue;

					if (!routeForward && seenBackward.contains(nextVertex))
						continue;

					double waitingTime = 0.0;

					if (nextVertex.getDistance(routeForward) == Double.POSITIVE_INFINITY) {
						nextVertex.setDistance(routeForward,
								(road.getRoadWeight(criteria) + beginVertex.getDistance(routeForward) + waitingTime));

						if (routeForward) {
							nextVertex.previousF = beginVertex.node;
						} else {
							nextVertex.previousB = beginVertex.node;
						}

						nextVertex.setForward(routeForward);

						nextVertex.connectingRoad = road;
						whichSet.add(nextVertex);

					} else {
						double dist = beginVertex.getDistance(routeForward) + road.getRoadWeight(criteria)
								+ waitingTime;
						if (dist < nextVertex.getDistance(routeForward)) {
							if (whichSet.contains(nextVertex))
								whichSet.remove(nextVertex);
							nextVertex.setDistance(routeForward, dist);
							if (routeForward) {
								nextVertex.previousF = beginVertex.node;
							} else {
								nextVertex.previousB = beginVertex.node;
							}
							nextVertex.setForward(routeForward);
							nextVertex.connectingRoad = road;
							whichSet.add(nextVertex);
						}
					}

				}
			}
			if (routeForward)
				seenForward.add(beginVertex);
			else
				seenBackward.add(beginVertex);

			routeForward = !routeForward;
			if (foundRoute)
				break;

		}

		// After the break condition is satisfied we need to find the vertex
		// which lies along the shortest path so as to reconstruct the shortest
		// route from source to destination

		Double minSum = w.minDistanceBackward + w.minDistanceForward;

		for (Vertex v : unseenForward) {
			if (v.isProcessedBackward) {
				double minDistance = v.minDistanceBackward + v.minDistanceForward;
				if (minDistance < minSum) {
					w = v;
					minSum = minDistance;
				}
			}

		}

		for (Vertex v : unseenBackward) {
			if (v.isProcessedForward) {
				double minDistance = v.minDistanceBackward + v.minDistanceForward;
				if (minDistance < minSum) {
					w = v;
					minSum = minDistance;
				}
			}
		}

		Vertex vp = w;

		while (true) {
			if (vp.node.getNodeId() == srcNode.getNodeId())
				break;

			path.add(vp.connectingRoad);
			vp = all.get(vp.previousF);
		}
		Collections.reverse(path);

		vp = w;

		while (true) {
			if (!path.contains(vp.connectingRoad))
				path.add(vp.connectingRoad);
			vp = all.get(vp.previousB);
			if (vp.node.getNodeId() == destNode.getNodeId())
				break;
		}

	}

	@Override
	public List<RoadNode> getRouteInNodes() {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String args[]) throws Exception {
		Properties dbConnectionProperties = new Properties();
		dbConnectionProperties.load(new FileInputStream("src/main/resources/connection.properties"));
		OSMRoadNetworkModel rnwModel = new OSMRoadNetworkModel(dbConnectionProperties, "osm_pbf.roads", "osm_pbf.nodes",
				6, "1.0", 5, false, true);

		BidirectionalDjikstra rd = new BidirectionalDjikstra(rnwModel);
		long t1 = System.currentTimeMillis();

		Road beginRoad = rnwModel.getAllRoadsMap().get(367740);
		Road endRoad = rnwModel.getAllRoadsMap().get(356513);
		RoadNode begin = beginRoad.getBeginNode();
		RoadNode end = endRoad.getEndNode();
		rd.djikstra(begin, end);

		System.out.println(
				"Execution time: " + (System.currentTimeMillis() - t1) + "ms visited node size: " + rd.visited.size());

		RoadNetworkVisualizer viewer = new OSMRoadNetworkViewer("Open-street map", rnwModel);
		viewer.getSelectedRoads().addAll(rd.getRoute());
		rd.reset();

	}

}
