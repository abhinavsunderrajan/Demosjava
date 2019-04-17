package networkmodel;

import java.io.Serializable;
import java.util.Set;
import java.util.TreeSet;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Class representing a node in the road network.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class RoadNode implements Serializable {

    /**
     * 
     */
    protected static final long serialVersionUID = 1L;
    protected Long nodeId;
    protected Set<Road> outRoads = new TreeSet<Road>();
    protected Set<Road> inRoads = new TreeSet<Road>();
    protected boolean isBeginOrEnd;
    protected Coordinate position;
    protected boolean trafficLight;

    /**
     * A road node.
     * 
     * @param nodeId
     * @param x
     * @param y
     */
    public RoadNode(long nodeId, double x, double y) {
	this.nodeId = nodeId;
	position = new Coordinate(x, y);
	isBeginOrEnd = false;
    }

    /**
     * @return the nodeId
     */
    public Long getNodeId() {
	return nodeId;
    }

    /**
     * @param nodeId the nodeId to set
     */
    public void setNodeId(long nodeId) {
	this.nodeId = nodeId;
    }

    /**
     * @return the x
     */
    public double getX() {
	return position.x;
    }

    /**
     * @param x the x to set
     */
    public void setX(double x) {
	position.x = x;
    }

    /**
     * @return the y
     */
    public double getY() {
	return position.y;
    }

    /**
     * @param y the y to set
     */
    public void setY(double y) {
	position.y = y;
    }

    /**
     * @return the outRoads
     */
    public Set<Road> getOutRoads() {
	return outRoads;
    }

    /**
     * @param outRoads the outRoads to set
     */
    public void setOutRoads(Set<Road> outRoads) {
	this.outRoads = outRoads;
    }

    /**
     * @return the inRoads
     */
    public Set<Road> getInRoads() {
	return inRoads;
    }

    public boolean isBeginOrEnd() {
	return isBeginOrEnd;
    }

    public void setBeginOrEnd(boolean isBeginOrEnd) {
	this.isBeginOrEnd = isBeginOrEnd;
    }

    /**
     * @param inRoads the inRoads to set
     */
    public void setInRoads(Set<Road> inRoads) {
	this.inRoads = inRoads;
    }

    public Coordinate getPosition() {
	return position;
    }

    /**
     * @param position the position to set
     */
    public void setPosition(Coordinate position) {
	this.position = position;
    }

    /**
     * A true flag indicates that the node is associated with a traffic light.
     * 
     * @return
     */
    public boolean isTrafficLight() {
	return trafficLight;
    }

    /**
     * 
     * @param trafficLight
     */
    public void setTrafficLight(boolean trafficLight) {
	this.trafficLight = trafficLight;
    }

    @Override
    public int hashCode() {
	return nodeId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof RoadNode))
	    return false;
	return this.nodeId.equals(((RoadNode) obj).nodeId);

    }

    @Override
    public String toString() {
	return nodeId + "";
    }

}
