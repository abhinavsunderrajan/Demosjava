package routing;

import java.util.ArrayList;
import java.util.function.Supplier;

import org.apache.commons.pool2.impl.GenericObjectPool;
import org.json.JSONArray;
import org.json.JSONObject;

import networkmodel.Road;
import networkmodel.RoadNode;

/**
 * Wrapper class for parallel route computations
 * 
 * @author abhinav.sunderrajan
 *
 */
public class GetShortestPath implements Supplier<JSONObject> {

    private RoadNode origin;
    private RoadNode destination;
    private Criteria criteria;
    private GenericObjectPool<Djikstra> pool;

    public GetShortestPath(RoadNode origin, RoadNode destination, Criteria criteria, GenericObjectPool<Djikstra> pool) {
	this.origin = origin;
	this.destination = destination;
	this.criteria = criteria;
	this.pool = pool;
    }

    @Override
    public JSONObject get() {
	JSONObject routingResult = null;

	try {
	    Djikstra dij = pool.borrowObject();
	    //dij.reset();
	    dij.setCriteria(criteria);
	    dij.djikstra(origin, destination);
	    routingResult = getRouteDetails(dij.getRoute(), dij.getDistanceDelta(), dij.isSameODNode());
	    pool.returnObject(dij);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return routingResult;
    }

    private JSONObject getRouteDetails(ArrayList<Road> route, double delta, boolean isSameODNode) {
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
		//	System.out.printf("original roadID = %d road dist = %.2f\n", road.getRoadId(), road.getLength());
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
	obj.put("origin_node", origin.getNodeId());
	obj.put("dest_node", destination.getNodeId());
	obj.put("distance", (distance + delta));
	obj.put("mean_time", expectedTime);
	obj.put("median_time", medianTime);
	obj.put("worst_case_time", worstCaseTime);
	obj.put("best_case_time", bestCaseTime);
	return obj;
    }

}