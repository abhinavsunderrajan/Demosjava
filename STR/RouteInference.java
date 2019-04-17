package utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.json.JSONArray;
import org.json.JSONObject;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

import networkmodel.Road;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;
import networkutils.GeoFunctions;
import route.BookingBean;
import route.RouteBean;
import routing.Criteria;
import routing.RoutingServiceDjikstraMT;

/**
 * Infer route from the trajectory.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class RouteInference {

    private static RoutingServiceDjikstraMT routing;
    private static RoadNetworkModel roadNetwork;
    private static final double LAMBDA = 0.2846547;
    private static final double CONFIDENCE_APE = 0.3;
    private static final double MIN_EMISSION_PROB = 1.0e-6;
    private static final double distInterval = 375.0;
    private static final ExponentialDistribution expHeading = new ExponentialDistribution(1.0 / LAMBDA);
    private static final ExponentialDistribution expDistance = new ExponentialDistribution(5.0);
    private static ExponentialDistribution expSpeed;
    private static boolean SPEED_PROB = true;
    private static boolean USE_HEADING = true;

    public static void setRoutingInstance(RoutingServiceDjikstraMT routingInstance) throws IOException {
	RouteInference.routing = routingInstance;
	roadNetwork = routingInstance.getRnwModel();
    }

    /**
     * Infer the difference between the estimated and return the difference
     * between the driven distance and the route distance computed from the GPS
     * trajectory.
     * 
     * @param booking
     * @return the routed distance in meters.
     */
    public static double inferRouteFromTrajectory(BookingBean booking) {

	double avgSpeed = booking.getDistance() * 1000
		/ ((booking.getDropOffTime().getMillis() - booking.getPickupTime().getMillis()) / 1000);
	expSpeed = new ExponentialDistribution(avgSpeed);

	List<JSONObject> selected = new ArrayList<JSONObject>();
	Coordinate prevLocation = booking.getPickupLocation();

	for (JSONObject obj : booking.getGPSDataPoints()) {
	    Coordinate location = new Coordinate(obj.getDouble("lon"), obj.getDouble("lat"));
	    double distance = GeoFunctions.haversineDistance(prevLocation, location);
	    if (distance > distInterval) {
		selected.add(obj);
		obj.put("used", true);
		prevLocation = location;
	    } else {
		obj.put("used", false);
	    }
	}
	double routedDistance = 0.0;

	if (selected.size() == 0)
	    selected.addAll(booking.getGPSDataPoints());

	try {

	    int currentIndex = -1;
	    int nextIndex = 0;
	    long t1 = 0;
	    long t2 = 0;
	    double speed1 = -1.0;
	    double speed2 = -1.0;

	    List<JSONObject> from;
	    List<JSONObject> to;
	    boolean finalDataPoint = false;

	    Map<Integer, List<JSONObject>> computedEmissions = new HashMap<>();

	    while (nextIndex <= selected.size()) {

		double straightLineDistance = 0.0;
		if (nextIndex == selected.size() && currentIndex == -1) {
		    t1 = booking.getPickupTime().getMillis() / 1000;
		    t2 = booking.getDropOffTime().getMillis() / 1000;
		    from = computedEmissions.containsKey(currentIndex) ? computedEmissions.get(currentIndex)
			    : computeClosestNodes(booking.getPickupLocation(), 20.0, 0.0);
		    to = computeClosestNodes(booking.getDropOffLocation(), 20.0, 0.0);
		    straightLineDistance = GeoFunctions.haversineDistance(booking.getPickupLocation(),
			    booking.getDropOffLocation());
		    finalDataPoint = true;

		} else if (currentIndex == -1) {
		    t1 = booking.getPickupTime().getMillis() / 1000;
		    t2 = selected.get(nextIndex).getLong("time_stamp");
		    from = computeClosestNodes(booking.getPickupLocation(), 20.0, 0.0);
		    to = getNearestNodes(selected.get(nextIndex));
		    straightLineDistance = GeoFunctions.haversineDistance(booking.getPickupLocation(), new Coordinate(
			    selected.get(nextIndex).getDouble("lon"), selected.get(nextIndex).getDouble("lat")));
		} else if (nextIndex == selected.size()) {
		    t1 = selected.get(currentIndex).getLong("time_stamp");
		    t2 = booking.getDropOffTime().getMillis() / 1000;
		    from = computedEmissions.containsKey(currentIndex) ? computedEmissions.get(currentIndex)
			    : getNearestNodes(selected.get(currentIndex));
		    to = computeClosestNodes(booking.getDropOffLocation(), 20.0, 0.0);
		    straightLineDistance = GeoFunctions.haversineDistance(booking.getDropOffLocation(), new Coordinate(
			    selected.get(currentIndex).getDouble("lon"), selected.get(currentIndex).getDouble("lat")));
		    finalDataPoint = true;
		} else {
		    t1 = selected.get(currentIndex).getLong("time_stamp");
		    t2 = selected.get(nextIndex).getLong("time_stamp");
		    from = computedEmissions.containsKey(currentIndex) ? computedEmissions.get(currentIndex)
			    : getNearestNodes(selected.get(currentIndex));
		    to = getNearestNodes(selected.get(nextIndex));
		    straightLineDistance = GeoFunctions.haversineDistance(
			    new Coordinate(selected.get(currentIndex).getDouble("lon"),
				    selected.get(currentIndex).getDouble("lat")),
			    new Coordinate(selected.get(nextIndex).getDouble("lon"),
				    selected.get(nextIndex).getDouble("lat")));
		}

		computedEmissions.put(currentIndex, from);
		computedEmissions.put(nextIndex, to);

		// No near roads found, may be map issue or GPS noise
		if (from.size() == 0) {
		    currentIndex++;
		    nextIndex++;
		    continue;
		}
		long timeDiff = t2 - t1;
		if (to.size() == 0 || timeDiff <= 0) {
		    nextIndex++;
		    continue;
		}

		JSONObject strResult = getMostProbableTransition(from, to, straightLineDistance, timeDiff,
			finalDataPoint);
		if (strResult == null || strResult.getJSONArray("route").length() == 0) {
		    nextIndex++;
		    continue;
		} else {
		    if (currentIndex >= 0 && nextIndex < selected.size()) {
			speed1 = selected.get(currentIndex).getDouble("speed");
			speed2 = selected.get(nextIndex).getDouble("speed");
		    }
		    currentIndex = nextIndex;
		    nextIndex++;
		    routedDistance += interpretSTRResult(booking, strResult, speed1, speed2, t1, t2) / 1000.0;
		}
	    }

	} catch (Exception e) {
	    e.printStackTrace();
	}

	return routedDistance;
    }

    private static List<JSONObject> getNearestNodes(JSONObject dataPoint) throws ParseException {
	Coordinate coord = new Coordinate(dataPoint.getDouble("lon"), dataPoint.getDouble("lat"));
	double accuracy = dataPoint.getDouble("accuracy");
	double bearing = dataPoint.getDouble("bearing");
	double speed = dataPoint.getDouble("speed");

	if (bearing > 0 && speed > 3.0)
	    return computeClosestNodes(coord, accuracy, bearing);
	else
	    return computeClosestNodes(coord, accuracy, 0.0);

    }

    /**
     * Compute the emission probability for the GPS data point.
     * 
     * @param coord
     *            the coordinate
     * @param accuracy
     *            accuracy
     * @param bearing
     *            bearing
     * @return
     * @throws ParseException
     */
    private static List<JSONObject> computeClosestNodes(Coordinate coord, double accuracy, double bearing)
	    throws ParseException {
	WKTReader wkt = new WKTReader(new GeometryFactory());
	double headingScore = 1.0;
	Point point = (Point) wkt.read("POINT (" + coord.x + " " + coord.y + ")");
	accuracy = accuracy < 10.0 ? 10.0 : accuracy;
	List<JSONObject> arr = new ArrayList<>();

	Set<Road> nearestRoads = roadNetwork.getNearestRoads(coord);

	for (Road road : nearestRoads) {
	    JSONObject obj = new JSONObject();
	    List<RoadNode> nodes = road.getRoadNodes();
	    double minDistance = Double.POSITIVE_INFINITY;
	    int closestSegment = 0;

	    for (int i = 1; i < nodes.size(); i++) {
		LineString ls = (LineString) wkt.read("LINESTRING  (" + nodes.get(i - 1).getX() + " "
			+ nodes.get(i - 1).getY() + ", " + nodes.get(i).getX() + " " + nodes.get(i).getY() + ")");

		double distance = ls.distance(point) * (Math.PI / 180) * 6378137;
		if (distance < minDistance) {
		    minDistance = distance;
		    closestSegment = i - 1;
		}
	    }

	    if (USE_HEADING && bearing > 0.0)
		headingScore = getHeadingLikelihood(road.getRoadNodes().get(closestSegment).getPosition(),
			road.getRoadNodes().get(closestSegment + 1).getPosition(), bearing);

	    obj.put("segment", closestSegment);
	    obj.put("road_id", road.getRoadId());

	    double emissionProb = (Math.exp(-(minDistance / accuracy) * (minDistance / accuracy) * 0.5))
		    / (accuracy * Math.sqrt(2 * Math.PI)) * headingScore;
	    obj.put("emission_probability", emissionProb);

	    // If emission probability is less than MIN_EMISSION_PROB it is
	    // highly unlikely that the GPS point lies on the node.

	    if (emissionProb > MIN_EMISSION_PROB) {
		if (arr.size() == 0) {
		    arr.add(obj);
		} else {
		    int index = arr.size() - 1;
		    while (index > -1 && emissionProb > arr.get(index).getDouble("emission_probability")) {
			index--;
		    }
		    arr.add(index + 1, obj);
		}
	    }

	}
	return arr;
    }

    private static double getHeadingLikelihood(Coordinate coord1, Coordinate coord2, double heading) {
	double bearing = GeoFunctions.bearing(coord1, coord2);
	return expHeading.density(Math.abs(heading - bearing));
    }

    /**
     * Returns the distance associated with route.
     * 
     * @param booking
     *            the associated booking for this routing result.
     * @param routingResult
     * @param speed2
     * @param speed1
     * @param t2
     * @param t1
     * @return
     * @throws IOException
     */
    private static double interpretSTRResult(BookingBean booking, JSONObject routingResult, double speed1,
	    double speed2, long t1, long t2) throws IOException {

	long increment = (t2 - t1) / routingResult.getJSONArray("route").length();
	long time = t1;
	double avgSpeedTransition = routingResult.getDouble("distance") / (t2 - t1);

	for (int i = 0; i < routingResult.getJSONArray("route").length(); i++) {
	    long roadId = routingResult.getJSONArray("route").getLong(i);
	    Road road = roadNetwork.getAllRoadsMap().get(roadId);

	    if (i == 0 && speed1 >= 0) {
		booking.getRoute().add(new RouteBean(road, time, speed1));
	    } else if (i == routingResult.getJSONArray("route").length() - 1 && speed2 >= 0) {
		booking.getRoute().add(new RouteBean(road, time, speed2));
	    } else {
		booking.getRoute().add(new RouteBean(road, time, avgSpeedTransition));
	    }

	    time += increment;

	}
	return routingResult.getDouble("distance");

    }

    private static JSONObject getMostProbableTransition(List<JSONObject> from, List<JSONObject> to,
	    double straightLinedistance, long timeDiff, boolean finalDataPoint)
	    throws InterruptedException, ExecutionException {
	Map<String, Future<JSONObject>> futures = new HashMap<>();
	Set<String> uniqueIds = new HashSet<>();

	int i = 0;
	for (JSONObject fromObj : from) {
	    Road closestRoad = roadNetwork.getAllRoadsMap().get(fromObj.getLong("road_id"));
	    int closestSegment = fromObj.getInt("segment");
	    RoadNode fromNode = closestRoad.getRoadNodes().get(closestSegment);
	    int j = 0;
	    for (JSONObject toObj : to) {
		closestRoad = roadNetwork.getAllRoadsMap().get(toObj.getLong("road_id"));
		closestSegment = toObj.getInt("segment");
		RoadNode toNode = closestRoad.getRoadNodes().get(closestSegment);
		if (fromNode.equals(toNode) || uniqueIds.contains(fromNode + "_" + toNode)) {
		    continue;
		}
		uniqueIds.add(fromNode + "_" + toNode);
		futures.put(i + "_" + j, routing.getRoute(fromNode, toNode, Criteria.TIME));
		j++;
	    }
	    i++;
	}

	double sum = 0.0;
	List<JSONObject> resultsList = new ArrayList<JSONObject>();
	uniqueIds.clear();

	for (Entry<String, Future<JSONObject>> future : futures.entrySet()) {
	    JSONObject routingResult = future.getValue().get();
	    if (routingResult.getJSONArray("route").length() == 0)
		continue;

	    String str = routingResult.getJSONArray("route").toString().replace("[", "").replace("]", "");
	    if (uniqueIds.contains(str)) {
		continue;
	    }
	    uniqueIds.add(str);

	    double routingDistance = routingResult.getDouble("distance");
	    double distanceProb = expDistance.density(Math.abs(routingDistance - straightLinedistance));
	    double speedProb = expSpeed.density(routingDistance / timeDiff);

	    double transitionProb = SPEED_PROB ? speedProb : distanceProb;

	    String split[] = future.getKey().split("_");
	    double hmmProb = transitionProb * from.get(Integer.parseInt(split[0])).getDouble("emission_probability")
		    * to.get(Integer.parseInt(split[1])).getDouble("emission_probability");
	    routingResult.put("hmm_prob", hmmProb);
	    sum += hmmProb;
	    resultsList.add(routingResult);
	}

	if (sum == 0.0) {
	    // System.out.println("no probable transition found..");
	    return null;
	}

	final double probSum = sum;
	resultsList.sort((o1, o2) -> ((Double) o2.getDouble("hmm_prob")).compareTo(o1.getDouble("hmm_prob")));

	resultsList.stream().forEach(obj -> {
	    double hmmProb = obj.getDouble("hmm_prob");
	    obj.put("hmm_prob", hmmProb / probSum);
	});

	double confidence = 100;
	if (resultsList.size() > 1)
	    confidence = (resultsList.get(0).getDouble("hmm_prob") - resultsList.get(1).getDouble("hmm_prob"))
		    / resultsList.get(0).getDouble("hmm_prob");

	if (confidence < CONFIDENCE_APE && !finalDataPoint) {
	    double routeSimilarity = compareForSimilarity(resultsList.get(0).getJSONArray("route"),
		    resultsList.get(1).getJSONArray("route"));
	    if (routeSimilarity >= 0.8) {
		return resultsList.get(0);
	    } else {
		// System.out.println("Equiprobable route hence undecided on
		// transition");
		return null;
	    }
	}
	return resultsList.get(0);
    }

    /**
     * Compare the two json arrays and return similarity in terms of substrings
     * found.
     * 
     * @param route1
     *            route1
     * @param route2
     *            route2
     * @return similarity score.
     */
    private static double compareForSimilarity(JSONArray route1, JSONArray route2) {

	// if the last matched roads are not equal then definitely something
	// fishy.
	if (route1.getInt(route1.length() - 1) != route2.getInt(route2.length() - 1))
	    return 0.0;

	JSONArray smaller;
	JSONArray bigger;
	if (route1.length() >= route2.length()) {
	    bigger = route1;
	    smaller = route2;
	} else {
	    bigger = route2;
	    smaller = route1;
	}

	int smallIndex = 0;
	int bigIndex = 0;
	boolean subStrFound = false;

	for (int i = 0; i < smaller.length(); i++) {
	    for (int j = 0; j < bigger.length(); j++) {
		if (bigger.getInt(j) == smaller.getInt(i)) {
		    smallIndex = i;
		    bigIndex = j;
		    subStrFound = true;
		    break;
		}
	    }
	    if (subStrFound)
		break;
	}

	if (!subStrFound)
	    return 0.0;

	int substr = 0;
	for (int i = smallIndex; i < smaller.length(); i++) {
	    if (smaller.getInt(i) == bigger.getInt(bigIndex)) {
		substr++;
		bigIndex++;
	    } else
		break;

	}

	return ((double) substr) / bigger.length();
    }

}
