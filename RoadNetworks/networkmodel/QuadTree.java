package networkmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

import networkutils.GeoFunctions;

/**
 * Partition a road network using QuadTrees.
 * {@link https://en.wikipedia.org/wiki/Quadtree}. Generally, the R tree spatial
 * indexing offers better performance, but QuadTree is not bad. Can replace this
 * with R-Tree if performance becomes an issue.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class QuadTree implements Comparable<QuadTree>, Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private static final int MAX_OBJECTS = 5000;
    private int level;
    private int nodeId;

    private HashSet<RoadNode> associatedNodes = new HashSet<RoadNode>();
    private ArrayList<RoadNode> retrieveListNodes = new ArrayList<RoadNode>();
    private HashSet<Road> associatedLinks = new HashSet<Road>();
    private Envelope bounds;

    public QuadTree[] children;
    private static Set<QuadTree> allLeaves = new TreeSet<QuadTree>();
    private static int nodeIdCounter = 0;

    /**
     * Construct a QuadTree with custom values. Used to create sub trees or
     * branches
     * 
     * @param l
     *            The level of this tree
     * @param b
     *            The bounds of this tree
     */
    public QuadTree(int l, Envelope b) {
	level = l;
	bounds = b;
	this.nodeId = ++nodeIdCounter;
	children = new QuadTree[4];
    }

    /**
     * @return the associatedNodes
     */
    public HashSet<RoadNode> getAssociatedNodes() {
	return associatedNodes;
    }

    /**
     * @return the associatedLinks
     */
    public HashSet<Road> getAssociatedRoads() {
	return associatedLinks;
    }

    /**
     * @return the bounds
     */
    public Envelope getBounds() {
	return bounds;
    }

    /**
     * The coordinates of the {@link Envelope} representing the tree.
     * 
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     */
    public void setBounds(double x1, double y1, double x2, double y2) {
	bounds = new Envelope(x1, x2, y1, y2);
	clear();
	split();
    }

    /**
     * Clear this tree. Also clears any subtrees.
     */
    public void clear() {
	associatedNodes.clear();
	for (int i = 0; i < children.length; i++) {
	    if (children[i] != null) {
		children[i].clear();
		children[i] = null;
	    }
	}
    }

    // Split the tree into 4 quadrants
    private void split() {

	double medianX = (bounds.getMaxX() + bounds.getMinX()) / 2;
	double medianY = (bounds.getMaxY() + bounds.getMinY()) / 2;

	children[0] = new QuadTree(level + 1, new Envelope(bounds.getMinX(), medianX, bounds.getMaxY(), medianY));
	children[1] = new QuadTree(level + 1, new Envelope(medianX, bounds.getMaxX(), bounds.getMaxY(), medianY));
	children[2] = new QuadTree(level + 1, new Envelope(bounds.getMinX(), medianX, medianY, bounds.getMinY()));
	children[3] = new QuadTree(level + 1, new Envelope(medianX, bounds.getMaxX(), medianY, bounds.getMinY()));
    }

    // Get the index of a rectangle
    private int getIndex(Envelope r) {
	int index = -1;
	double medianX = (bounds.getMaxX() + bounds.getMinX()) / 2;
	double medianY = (bounds.getMaxY() + bounds.getMinY()) / 2;

	boolean topQuadrant = (r.getMinY() > medianY) ? true : false;
	if (r.getMinX() < medianX) {
	    if (topQuadrant) {
		index = 0;
	    } else {
		index = 2;
	    }
	} else {
	    if (topQuadrant) {
		index = 1;
	    } else {
		index = 3;
	    }
	}
	return index;
    }

    // Get the index of an object
    private int getIndex(RoadNode node) {
	int index = -1;
	double medianX = (bounds.getMaxX() + bounds.getMinX()) / 2;
	double medianY = (bounds.getMaxY() + bounds.getMinY()) / 2;

	boolean topQuadrant = (node.getY() > medianY) ? true : false;
	if (node.getX() < medianX) {
	    if (topQuadrant) {
		index = 0;
	    } else {
		index = 2;
	    }
	} else {
	    if (topQuadrant) {
		index = 1;
	    } else {
		index = 3;
	    }
	}
	return index;
    }

    /**
     * Insert an {@link RoadNode} into this tree
     * 
     * @param node
     */
    public void insert(RoadNode node) {
	if (children[0] != null) {
	    int index = getIndex(node);
	    if (index != -1) {
		children[index].insert(node);
		return;
	    }
	}
	associatedNodes.add(node);
	associatedLinks.addAll(node.getInRoads());
	associatedLinks.addAll(node.getOutRoads());

	if (associatedNodes.size() > MAX_OBJECTS) {
	    if (children[0] == null) {
		split();
	    }
	    for (RoadNode linkNode : associatedNodes) {
		int index = getIndex(linkNode);
		if (index != -1) {
		    children[index].insert(linkNode);
		}
	    }
	}
    }

    /**
     * Insert an ArrayList of objects into this tree
     */
    public void insert(ArrayList<RoadNode> o) {
	for (int i = 0; i < o.size(); i++) {
	    insert(o.get(i));
	}
    }

    /**
     * Return nodes associated with the envelope passed as the argument.
     * 
     * @param r
     * @return
     */
    public ArrayList<RoadNode> retrieveNodes(Envelope r) {
	retrieveListNodes.clear();
	int index = getIndex(r);
	if (index != -1 && children[0] != null) {
	    retrieveListNodes = children[index].retrieveNodes(r);
	}
	retrieveListNodes.addAll(associatedNodes);
	return retrieveListNodes;
    }

    /**
     * Return all leaves associated with the created QuadTree.
     * 
     * @return
     */
    public Set<QuadTree> getAllLeaves() {
	for (int i = 0; i < children.length; i++) {
	    if (children[i] != null) {
		children[i].getAllLeaves();
	    } else {
		if (associatedNodes.size() > 0)
		    allLeaves.add(this);
	    }
	}
	return allLeaves;
    }

    /**
     * Return the leaf node of the QuadTree this coordinate is associated with.
     * 
     * @param coord
     * @return Smallest leaf {@link Envelope} which contains the coordinate
     */
    public QuadTree getLeafPartition(Coordinate coord) {
	for (int i = 0; i < children.length; i++) {
	    if (children[i] != null) {
		if (children[i].bounds.contains(coord)) {
		    return children[i].getLeafPartition(coord);
		}
	    }

	}
	return this;

    }

    /**
     * Return the list of closest leaf neighbors to the argument tree passed.
     * 
     * @param tree
     * @param numOfNeighbours
     * @return
     */
    public List<QuadTree> getAllNeighbours(QuadTree tree, int numOfNeighbours) {
	Envelope inQuestion = tree.getBounds();
	if (allLeaves.size() == 0) {
	    getAllLeaves();
	}

	Coordinate coord = new Coordinate(inQuestion.getMinX(), inQuestion.getMaxY());
	List<Double> distances = new ArrayList<>();
	List<QuadTree> neighbours = new ArrayList<>();
	for (QuadTree closest : allLeaves) {
	    double distance = GeoFunctions.haversineDistance(coord,
		    new Coordinate(closest.getBounds().getMinX(), closest.getBounds().getMaxY()));
	    if (distance > 0) {
		if (distances.size() == 0) {
		    distances.add(distance);
		    neighbours.add(closest);
		} else {

		    int index = distances.size() - 1;
		    while (index > -1 && distance < distances.get(index)) {
			index--;
		    }
		    distances.add(index + 1, distance);
		    neighbours.add(index + 1, closest);
		}

	    }

	}

	List<QuadTree> closest = new ArrayList<>();
	for (int i = 0; i < numOfNeighbours; i++) {
	    closest.add(neighbours.get(i));
	}

	return closest;

    }

    /**
     * Return all neighbors within the specified distance range.
     * 
     * @param tree
     * @param radius
     *            distance in meters.
     * @return
     */
    public Set<QuadTree> getAllNeighbours(QuadTree tree, double radius) {

	Envelope inQuestion = tree.getBounds();

	if (allLeaves.size() == 0) {
	    getAllLeaves();
	}

	Coordinate[] bounds1 = { new Coordinate(inQuestion.getMinX(), inQuestion.getMaxY()),
		new Coordinate(inQuestion.getMaxX(), inQuestion.getMinY()),
		new Coordinate(inQuestion.getMinX(), inQuestion.getMinY()),
		new Coordinate(inQuestion.getMaxX(), inQuestion.getMaxY()) };

	Set<QuadTree> neighbours = new HashSet<>();

	for (QuadTree closest : allLeaves) {

	    // Do not include yourself as the neighbor
	    if (inQuestion.getMinX() == closest.getBounds().getMinX()
		    && inQuestion.getMaxY() == closest.getBounds().getMaxY()) {
		continue;

	    }

	    Coordinate[] bounds2 = { new Coordinate(closest.getBounds().getMinX(), closest.getBounds().getMaxY()),
		    new Coordinate(closest.getBounds().getMaxX(), closest.getBounds().getMinY()),
		    new Coordinate(closest.getBounds().getMinX(), closest.getBounds().getMinY()),
		    new Coordinate(closest.getBounds().getMaxX(), closest.getBounds().getMaxY()) };

	    boolean done = false;
	    for (int i = 0; i < bounds1.length; i++) {
		for (int j = 0; j < bounds2.length; j++) {
		    double distance = GeoFunctions.haversineDistance(bounds1[i], bounds2[j]);
		    if (distance < radius) {
			neighbours.add(closest);
			done = true;
			break;
		    }

		}

		if (done) {
		    break;
		}

	    }

	}

	return neighbours;

    }

    /**
     * Retrieve a set of all border nodes.
     * 
     * @return
     */
    public Set<RoadNode> getAllBorderNodes() {

	Set<RoadNode> borderNodes = new HashSet<>();
	for (Road link : associatedLinks) {
	    Coordinate fCoord = new Coordinate(link.getBeginNode().getX(), link.getBeginNode().getY());
	    Coordinate tCoord = new Coordinate(link.getEndNode().getX(), link.getEndNode().getY());

	    if (!bounds.contains(fCoord) && bounds.contains(tCoord)) {
		borderNodes.add(link.getEndNode());

	    }

	}

	return borderNodes;

    }

    /**
     * @return the nodeId
     */
    public int getNodeId() {
	return nodeId;
    }

    @Override
    public int compareTo(QuadTree o) {
	return this.nodeId - o.nodeId;
    }

}
