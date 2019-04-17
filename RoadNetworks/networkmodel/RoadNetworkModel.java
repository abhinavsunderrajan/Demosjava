package networkmodel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.io.GeohashUtils;
import com.spatial4j.core.shape.Rectangle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import networkutils.DatabaseAccess;
import networkutils.GeoFunctions;
import networkutils.SRandom;

/**
 * Abstract Road network model class. The method for loading the nodes and roads
 * has to be over-ridden by the concrete implementing class. The class also
 * contains some commonly used utility methods for modifying data in the
 * Database tables constituting the road-network.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public abstract class RoadNetworkModel {

    protected Map<Long, RoadNode> allNodesMap;
    protected Map<Long, Road> allRoadsMap;
    protected String roadDataTable;
    protected String nodeDataTable;
    protected Set<RoadNode> beginAndEndNodes;
    protected DatabaseAccess access;
    protected Properties dbConnectionProperties;
    protected Envelope cityBounds;
    protected Map<String, GeoHash> geoHashMap;
    protected int geoHashPrecision;

    /**
     * Load road network form database.
     * 
     * @param dbConnectionProperties
     * @param roadDataTable
     * @param nodeDataTable
     */
    public RoadNetworkModel(Properties dbConnectionProperties, String roadDataTable, String nodeDataTable,
	    int geoHashPrecision) {
	this.geoHashPrecision = geoHashPrecision;
	this.dbConnectionProperties = dbConnectionProperties;
	allNodesMap = new LinkedHashMap<Long, RoadNode>();
	allRoadsMap = new LinkedHashMap<Long, Road>();
	this.roadDataTable = roadDataTable;
	this.nodeDataTable = nodeDataTable;
	beginAndEndNodes = new HashSet<RoadNode>();
	geoHashMap = new HashMap<String, GeoHash>();
	access = new DatabaseAccess(dbConnectionProperties);
    }

    /**
     * Load road network form csv File.
     * 
     * @param dbConnectionProperties
     * @param roadDataTable
     * @param nodeDataTable
     */
    public RoadNetworkModel(int geoHashPrecision) {
	this.geoHashPrecision = geoHashPrecision;
	allNodesMap = new HashMap<Long, RoadNode>();
	allRoadsMap = new HashMap<Long, Road>();
	beginAndEndNodes = new HashSet<RoadNode>();
	geoHashMap = new HashMap<String, GeoHash>();
    }

    /**
     * Override this method to load roads and nodes as per {@link Road} and
     * {@link RoadNode} class models.
     * 
     * @param mapversion the map version to load
     * 
     * @throws Exception
     */
    protected abstract void loadNodesAndRoadsFromDB(String mapVersion) throws Exception;

    /**
     * @return the allNodes
     */
    public Map<Long, RoadNode> getAllNodes() {
	return allNodesMap;
    }

    /**
     * @return the beginAndEndNodes
     */
    public Set<RoadNode> getBeginAndEndNodes() {
	return beginAndEndNodes;
    }

    public Map<Long, Road> getAllRoadsMap() {
	return allRoadsMap;
    }

    public Properties getDbConnectionProperties() {
	return dbConnectionProperties;
    }

    /**
     * Get the geohash map.
     * 
     * @return
     */
    public Map<String, GeoHash> getGeoHashMap() {
	return geoHashMap;
    }

    public void setCityBounds(Envelope cityBounds) {
	this.cityBounds = cityBounds;
    }

    /**
     * Get the bounds for the city
     * 
     * @return the city bound.
     */
    public Envelope getCityBounds() {
	return cityBounds;
    }

    /**
     * Returns the closest nodes to the coordinate. Checks in current and
     * neighboring geohash.
     * 
     * @param coord the location
     * @return road and node contained in an object array.
     */
    public List<RoadNode> getNearestNodesSortedByDistance(Coordinate coord) {
	String geohashId = GeohashUtils.encodeLatLon(coord.y, coord.x, geoHashPrecision);
	List<RoadNode> candidateNodes = new ArrayList<RoadNode>();

	if (geoHashMap.containsKey(geohashId))
	    candidateNodes.addAll(geoHashMap.get(geohashId).getAssociatedNodes());

	for (GeoHash geoHash : getNeighboringGeoHashes(geohashId))
	    candidateNodes.addAll(geoHash.getAssociatedNodes());

	Map<Double, RoadNode> ordered = new TreeMap<Double, RoadNode>();
	for (RoadNode node : candidateNodes) {
	    double distance = GeoFunctions.haversineDistance(coord, node.getPosition());
	    ordered.put(distance, node);
	}
	List<RoadNode> closestNodes = new ArrayList<RoadNode>();
	for (RoadNode node : ordered.values())
	    closestNodes.add(node);

	return closestNodes;
    }

    /**
     * Returns the nearest roads to this coordinate by checking the current and
     * Neighboring geohashes.
     * 
     * @param coord
     * @return an unordered set of closest roads to this coordinate.
     */
    public Set<Road> getNearestRoads(Coordinate coord) {
	String geohashId = GeohashUtils.encodeLatLon(coord.y, coord.x, geoHashPrecision);
	Set<Road> candidateRoads = new HashSet<Road>();

	if (geoHashMap.containsKey(geohashId))
	    candidateRoads.addAll(geoHashMap.get(geohashId).getAssociatedRoads());

	for (GeoHash geoHash : getNeighboringGeoHashes(geohashId))
	    candidateRoads.addAll(geoHash.getAssociatedRoads());
	return candidateRoads;
    }

    /**
     * Get the neighboring geohashes for the unique geohashId passed. Note that 8
     * closest neighbors are checked. But if a neighboring geohash does not contain
     * any roads, the it will not be returned since it serves no point.
     * 
     * @param geohashId
     * @return the set of neighbors.
     */
    public Set<GeoHash> getNeighboringGeoHashes(String geohashId) {
	GeoHash geoHash = geoHashMap.get(geohashId);
	Rectangle bounds = null;

	if (geoHash != null)
	    bounds = geoHash.getBounds();
	else
	    bounds = GeohashUtils.decodeBoundary(geohashId, SpatialContext.GEO);
	List<Coordinate> boundaries = new ArrayList<Coordinate>();
	boundaries.add(new Coordinate(bounds.getMinX(), bounds.getMaxY()));
	boundaries.add(new Coordinate(bounds.getMaxX(), bounds.getMaxY()));
	boundaries.add(new Coordinate(bounds.getMinX(), bounds.getMinY()));
	boundaries.add(new Coordinate(bounds.getMaxX(), bounds.getMinY()));
	Set<GeoHash> neighbors = new HashSet<GeoHash>();
	double bearings[] = { 45, 135, 225, 315 };
	for (Coordinate coord : boundaries) {
	    for (double bearing : bearings) {
		Coordinate newCoord = GeoFunctions.getPointAtDistanceAndBearing(coord, 15.0,
			(bearing * Math.PI / 180.0));
		String neighborId = GeohashUtils.encodeLatLon(newCoord.y, newCoord.x, geoHashPrecision);
		if (!neighborId.equals(geohashId) && geoHashMap.containsKey(neighborId)) {
		    GeoHash neighbor = geoHashMap.get(neighborId);
		    if (neighbor.getAssociatedRoads().size() > 0)
			neighbors.add(neighbor);
		}
	    }
	}

	// if (neighbors.size() == 0) {
	// throw new IllegalStateException("A geo hash must have a neighbor");
	// }
	return neighbors;

    }

    public int getGeoHashPrecision() {
	return geoHashPrecision;
    }

    public void setGeoHashPrecision(int geoHashPrecision) {
	this.geoHashPrecision = geoHashPrecision;
    }

    /**
     * This is special case when some geohashes do not have any nodes hence not
     * encoded at all in the geohash map. Hence if the geohash string is not present
     * we go for the nearest node and its geo-hash.
     * 
     * @param currentLocation
     * @return the encoded geohash string.
     */
    public String encodeLocation(Coordinate currentLocation) {
	String currentGeoHash = GeohashUtils.encodeLatLon(currentLocation.y, currentLocation.x, geoHashPrecision);
	if (geoHashMap.containsKey(currentGeoHash))
	    return currentGeoHash;
	else {
	    Coordinate nodePos = getNearestNodesSortedByDistance(currentLocation).get(0).getPosition();
	    currentGeoHash = GeohashUtils.encodeLatLon(nodePos.y, nodePos.x, geoHashPrecision);
	    assert (geoHashMap.containsKey(currentGeoHash));
	}

	return currentGeoHash;

    }

    /**
     * Return a random node in the geohashId passed as the parameter.
     * 
     * @param geoHashId
     * @param seed      seed for {@link SRandom} instance.
     * @return randomNode.
     */
    public RoadNode getRandomNodeinGeoHash(String geoHashId, int seed) {
	int j = 0;
	GeoHash neighborGH = geoHashMap.get(geoHashId);
	SRandom random = SRandom.instance(seed);
	RoadNode randomNode = null;

	int roadIndex = random.nextInt(neighborGH.getAssociatedNodes().size());
	for (RoadNode destinationNode : neighborGH.getAssociatedNodes()) {
	    if (j == roadIndex) {
		randomNode = destinationNode;
		break;
	    }
	    j++;
	}

	return randomNode;

    }

    /**
     * Get a random node to in the neighborhood of the geohash
     * 
     * @param geoHashId the geohashid
     * @param seed      the seed for random instance.
     * @return the random {@link RoadNode} in the neighborhood.
     */
    public RoadNode getRandomNodeInGeoHashNeighborHood(String geoHashId, int seed) {
	Set<GeoHash> neighbours = getNeighboringGeoHashes(geoHashId);
	SRandom random = SRandom.instance(seed);
	int neighbourIndex = random.nextInt(neighbours.size());
	int i = 0;
	RoadNode randomNode = null;
	for (GeoHash neighborGH : neighbours) {
	    if (i == neighbourIndex) {
		int j = 0;
		int roadIndex = random.nextInt(neighborGH.getAssociatedNodes().size());
		for (RoadNode destinationNode : neighborGH.getAssociatedNodes()) {
		    if (j == roadIndex) {
			randomNode = destinationNode;
			break;
		    }
		    j++;
		}
		break;
	    }
	    i++;

	}
	return randomNode;

    }

    /**
     * return the database connection.
     * 
     * @return
     */
    public DatabaseAccess getDatabaseAccess() {
	return access;
    }

    /**
     * Compute the angle between road 1 and road 2 at the given node
     * 
     * @param road1 the first road
     * @param road2
     * @param node
     * @return
     */
    public static double computeAngle(Road road1, Road road2, RoadNode node) {
	double angle = 0.0;
	double bearing1 = -1.0;

	int numNodes = road1.getRoadNodes().size();

	if (road1.getEndNode().equals(node)) {
	    bearing1 = GeoFunctions.bearing(road1.getRoadNodes().get(numNodes - 2).getPosition(),
		    road1.getRoadNodes().get(numNodes - 1).getPosition());
	} else {
	    for (int i = 0; i < road1.getRoadNodes().size() - 1; i++) {
		if (road1.getRoadNodes().get(i).equals(node))
		    bearing1 = GeoFunctions.bearing(road1.getRoadNodes().get(i).getPosition(),
			    road1.getRoadNodes().get(i + 1).getPosition());
	    }
	}

	double bearing2 = -1;
	numNodes = road2.getRoadNodes().size();
	if (road2.getEndNode().equals(node)) {
	    bearing2 = GeoFunctions.bearing(road2.getRoadNodes().get(numNodes - 1).getPosition(),
		    road2.getRoadNodes().get(numNodes - 2).getPosition());
	} else if (road2.getBeginNode().equals(node)) {
	    bearing2 = GeoFunctions.bearing(road2.getRoadNodes().get(0).getPosition(),
		    road2.getRoadNodes().get(1).getPosition());
	} else {
	    for (int i = 0; i < road2.getRoadNodes().size() - 1; i++) {
		if (road2.getRoadNodes().get(i).equals(node))
		    bearing2 = GeoFunctions.bearing(road2.getRoadNodes().get(i).getPosition(),
			    road2.getRoadNodes().get(i + 1).getPosition());
	    }
	}

	if (bearing1 < 0 || bearing2 < 0)
	    throw new IllegalArgumentException("The roads " + road1 + " and " + road2 + " do not intersect at " + node);

	// angle = Math.abs(bearing2 - bearing1);
	angle = (bearing2 - bearing1) < 0 ? 360.0 + (bearing2 - bearing1) : (bearing2 - bearing1);

	return angle;
    }

}
