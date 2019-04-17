package networkmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import networkutils.GeoFunctions;
import routing.Criteria;

/**
 * Class representing a road of the network.
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class Road implements Serializable, Comparable<Road> {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private Long roadId;
    private int laneCount;
    private boolean oneWay;
    private String name;
    private List<RoadNode> roadNodes;
    private String roadType;
    private RoadNode beginNode;
    private RoadNode endNode;
    private String kind;
    private int roadClass;
    private double[] segmentLengths;
    private double roadLength;
    private boolean hasSpeedInfo;

    private double expectedSpeed;
    private double lowerBoundSpeed;
    private double upperBoundSpeed;
    private double medianSpeed;

    private long numOfPings;

    private boolean isTunnel;

    /**
     * @return the roadClass
     */
    public int getRoadClass() {
	return roadClass;
    }

    /**
     * @param roadClass the roadClass to set
     */
    public void setRoadClass(int roadClass) {
	this.roadClass = roadClass;
    }

    /**
     * The road network
     * 
     * @param wayId the way id from openstreet map binary. file
     */
    public Road(long wayId) {
	this.roadId = wayId;
	this.roadNodes = new ArrayList<>();
	oneWay = true;
    }

    /**
     * @return the roadId
     */
    public Long getRoadId() {
	return roadId;
    }

    /**
     * @param roadId the roadId to set
     */
    public void setRoadId(long roadId) {
	this.roadId = roadId;
    }

    /**
     * @return the intermediateNodes
     */
    public List<RoadNode> getRoadNodes() {
	return roadNodes;
    }

    /**
     * @param roadNodes the nodes to set
     */
    public void setRoadNodes(List<RoadNode> roadNodes) {
	this.roadNodes = roadNodes;
	computeLength();
    }

    /**
     * @return the lanes
     */
    public int getLaneCount() {
	return laneCount;
    }

    /**
     * @param lanes the lanes to set
     */
    public void setLaneCount(int lanes) {
	this.laneCount = lanes;
    }

    /**
     * @return the oneWay
     */
    public boolean isOneWay() {
	return oneWay;
    }

    /**
     * @param oneWay the oneWay to set
     */
    public void setOneWay(boolean oneWay) {
	this.oneWay = oneWay;
    }

    /**
     * @return the name
     */
    public String getName() {
	return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
	this.name = name;
    }

    /**
     * @return the roadType
     */
    public String getRoadType() {
	return roadType;
    }

    /**
     * @param roadType the roadType to set
     */
    public void setRoadType(String roadType) {
	this.roadType = roadType;
    }

    /**
     * @return the beginNode
     */
    public RoadNode getBeginNode() {
	return beginNode;
    }

    /**
     * @param beginNode the beginNode to set
     */
    public void setBeginNode(RoadNode beginNode) {
	this.beginNode = beginNode;
    }

    /**
     * @return the endNode
     */
    public RoadNode getEndNode() {
	return endNode;
    }

    /**
     * @param endNode the endNode to set
     */
    public void setEndNode(RoadNode endNode) {
	this.endNode = endNode;
    }

    /**
     * Kind of road Represents Interchange Roads connecting 2 expressway. Junction
     * Links within a junction. Slip Road Roads going out of expressway Ramps Roads
     * leading to expressway.
     * 
     * @return the kind
     */
    public String getKind() {
	return kind;
    }

    /**
     * @param kind the kind to set
     */
    public void setKind(String kind) {
	this.kind = kind;
    }

    /**
     * Returns the weight of the road based on the {@link Criteria} give.
     * 
     * @param criteria
     * @param surge
     * @return
     */
    public double getRoadWeight(Criteria criteria) {
	double weight = 0.0;

	switch (criteria) {
	case DISTANCE:
	    weight = roadLength;
	    break;
	default:
	    weight = getTravelTimeInSec();
	    break;
	}
	return weight;
    }

    /**
     * Returns the length of the road.
     * 
     * @return length of road in meters.
     */
    public double getLength() {
	return roadLength;
    }

    /**
     * Computes the length of the road using haversine distance.
     * 
     */
    private void computeLength() {
	double len = 0.0;
	segmentLengths = new double[roadNodes.size() - 1];
	for (int i = 1; i < roadNodes.size(); i++) {
	    double segmentLength = GeoFunctions.haversineDistance(roadNodes.get(i).getY(), roadNodes.get(i - 1).getY(),
		    roadNodes.get(i).getX(), roadNodes.get(i - 1).getX());
	    segmentLengths[i - 1] = segmentLength;
	    len += segmentLength;
	}
	this.roadLength = len;
    }

    /**
     * Get expected speed in m/s
     * 
     * @return
     */
    public double getExpectedSpeed() {
	return expectedSpeed;
    }

    /**
     * Sets the expected speed of the road in m/s.
     * 
     * @param speed
     */
    public void setExpectedSpeed(double speed) {
	this.expectedSpeed = speed;
    }

    @Override
    public int hashCode() {
	return roadId.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
	if (!(obj instanceof Road))
	    return false;

	return this.roadId.equals(((Road) obj).roadId);

    }

    @Override
    public String toString() {
	return this.roadId + "";
    }

    /**
     * Returns the lengths of all segments constituting the road.
     * 
     * @return
     */
    public double[] getSegmentsLength() {
	return segmentLengths;
    }

    /**
     * Returns the travel time across the road in seconds.
     */
    public double getTravelTimeInSec() {
	return roadLength / expectedSpeed;
    }

    public boolean isHasSpeedInfo() {
	return hasSpeedInfo;
    }

    public void setHasSpeedInfo(boolean hasSpeedInfo) {
	this.hasSpeedInfo = hasSpeedInfo;
    }

    public double getLowerBoundSpeed() {
	return lowerBoundSpeed;
    }

    public void setLowerBoundSpeed(double lowerBoundSpeed) {
	this.lowerBoundSpeed = lowerBoundSpeed;
    }

    public double getUpperBoundSpeed() {
	return upperBoundSpeed;
    }

    public void setUpperBoundSpeed(double upperBoundSpeed) {
	this.upperBoundSpeed = upperBoundSpeed;
    }

    public double getMedianSpeed() {
	return medianSpeed;
    }

    public void setMedianSpeed(double medianSpeed) {
	this.medianSpeed = medianSpeed;
    }

    @Override
    public int compareTo(Road o) {
	// TODO Auto-generated method stub
	return Long.compare(this.roadId, o.getRoadId());
    }

    /**
     * Get the number of pings recored on this road. Used purely for analysis
     * purposes
     * 
     * @return
     */
    public long getNumOfPings() {
	return numOfPings;
    }

    /**
     * Set the number of pings recored on this road. Used purely for analysis
     * purposes
     * 
     * @param numOfPings
     */
    public void setNumOfPings(long numOfPings) {
	this.numOfPings = numOfPings;
    }

    public boolean isTunnel() {
	return isTunnel;
    }

    public void setTunnel(boolean isTunnel) {
	this.isTunnel = isTunnel;
    }

}
