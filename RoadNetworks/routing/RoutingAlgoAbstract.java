package routing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import networkmodel.Road;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;

/**
 * Abstract routing class any routing algorithm and any network i.e
 * {@link RoadNetworkModel} should extend this class and
 * 
 * @author abhinav.sunderrajan
 *
 */
public abstract class RoutingAlgoAbstract {

    // Forward search
    protected Set<Vertex> seenForward = new HashSet<Vertex>();
    protected TreeSet<Vertex> unseenForward = new TreeSet<Vertex>();
    // Backward search
    protected Set<Vertex> seenBackward = new HashSet<Vertex>();
    protected TreeSet<Vertex> unseenBackward = new TreeSet<Vertex>();

    protected Map<RoadNode, Vertex> all = new HashMap<RoadNode, Vertex>();
    protected Set<Vertex> visited = new HashSet<Vertex>();
    protected List<Vertex> routeNodes = new ArrayList<Vertex>();
    protected Criteria criteria;
    protected Vertex w;
    protected ArrayList<Road> path;
    // Distance delta is to take into account begin-node end-node resolution.
    // Add this distance delta which will give give an approximately better that
    // has been routed.
    protected double distanceDelta = 0.0;
    protected boolean sameODNode = false;

    /**
     * Initialize with the road network model for normal routing using Dijkstra.
     * 
     * @param rnwModel
     * @throws ParseException
     * @throws IOException
     * @throws FileNotFoundException
     */
    public RoutingAlgoAbstract(RoadNetworkModel rnwModel) throws FileNotFoundException, IOException {

	for (RoadNode node : rnwModel.getBeginAndEndNodes())
	    all.put(node, new Vertex(node));

	criteria = Criteria.DISTANCE;
	path = new ArrayList<Road>();

    }

    /**
     * Compute the shortest path from srcNode to the destNode.
     * 
     * @param srcNode
     * @param destNode
     */
    public abstract void djikstra(RoadNode srcNode, RoadNode destNode);

    /**
     * Returns the criteria for routing.
     * 
     * @return
     */
    public Criteria getCriteria() {
	return criteria;
    }

    /**
     * Sets the criteria for routing.
     * 
     * @param criteria
     */
    public void setCriteria(Criteria criteria) {
	this.criteria = criteria;
    }

    /**
     * @return the unseen
     */
    public Set<Vertex> getUnseen() {
	return unseenForward;
    }

    /**
     * Call to reset after every invocation of Dijkstra.
     */
    public void reset() {

	distanceDelta = 0;

	if (visited.size() > 0) {
	    for (Vertex wrapper : visited) {
		wrapper.minDistanceForward = Double.POSITIVE_INFINITY;
		wrapper.previousF = null;
		wrapper.previousB = null;
	    }

	}
	unseenForward.clear();
	seenForward.clear();
	visited.clear();
	path.clear();
	routeNodes.clear();
	sameODNode = false;

    }

    /**
     * Return the route in terms of roads.
     * 
     * @return
     */
    public ArrayList<Road> getRoute() {
	return path;
    }

    /**
     * Add the distance delta to the actual distance computed from routing. This
     * is to resolve routing between intermediate nodes.
     * 
     * @return
     */
    public double getDistanceDelta() {
	return distanceDelta;
    }

    /**
     * returns true if the origin and destination nodes are the same. This must
     * be checked for if the route is unavailable for an od pair.
     * 
     * @return
     */
    public boolean isSameODNode() {
	return sameODNode;
    }

    /**
     * Return the route in terms of nodes
     * 
     * @return
     */
    public abstract List<RoadNode> getRouteInNodes();

}
