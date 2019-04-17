package routing;

import networkmodel.Road;
import networkmodel.RoadNode;

/**
 * The wrapper class for all routing
 * 
 * @author abhinav.sunderrajan
 *
 */
public class Vertex implements Comparable<Vertex> {

    double minDistanceForward = Double.POSITIVE_INFINITY;
    double minDistanceBackward = Double.POSITIVE_INFINITY;
    RoadNode previousF;
    RoadNode previousB;
    RoadNode node;
    Road connectingRoad;
    boolean isProcessedForward;
    boolean isProcessedBackward;

    private boolean isForward;

    public Vertex(RoadNode node) {
	this.node = node;
	this.isProcessedForward = false;
	this.isProcessedBackward = false;
    }

    @Override
    public int hashCode() {
	return node.getNodeId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Vertex))
	    return false;
	if (obj == this)
	    return true;
	return this.node.equals(((Vertex) obj).node);

    }

    @Override
    public int compareTo(Vertex other) {

	if (isForward()) {
	    if (this.minDistanceForward == other.minDistanceForward)
		return node.getNodeId().compareTo(other.node.getNodeId());
	    else
		return Double.compare(minDistanceForward, other.minDistanceForward);
	} else {
	    if (this.minDistanceBackward == other.minDistanceBackward)
		return node.getNodeId().compareTo(other.node.getNodeId());
	    else
		return Double.compare(minDistanceBackward, other.minDistanceBackward);
	}
    }

    public void setDistance(boolean isForward, double dist) {
	if (isForward)
	    minDistanceForward = dist;
	else
	    minDistanceBackward = dist;
    }

    public double getDistance(boolean isForward) {
	if (isForward)
	    return minDistanceForward;
	else
	    return minDistanceBackward;
    }

    public boolean isForward() {
	return isForward;
    }

    public void setForward(boolean isForward) {
	this.isForward = isForward;
    }

}
