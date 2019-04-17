package networkmodel;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import com.spatial4j.core.context.SpatialContext;
import com.spatial4j.core.io.GeohashUtils;
import com.spatial4j.core.shape.Rectangle;

/**
 * Representation of a geohash in the simulation. Other than spatial indexing,
 * geohash is critical in the simulation for pricing purposes. Hence pricing
 * specific class variables are added here as well
 *
 * @author abhinav.sunderrajan
 *
 */
public class GeoHash {

    private String uniqueId;
    private HashSet<RoadNode> associatedNodes;
    private HashSet<Road> associatedLinks;
    private Rectangle bounds;
    private int precision;

    // surge price specific parameters.
    private double demandMet;
    private double demandUnMet;
    private double supplyMet;
    private double ignoreDax;
    private double availableDax;
    private Map<Integer, Double> surgeMap;
    private static final double ignoredDiscountRate = 0.7;

    /**
     * A geohash with the unique ID and precision.
     *
     * @param uniqueId
     * @param precision
     */
    public GeoHash(String uniqueId, int precision) {
	this.uniqueId = uniqueId;
	this.bounds = GeohashUtils.decodeBoundary(uniqueId, SpatialContext.GEO);
	associatedNodes = new HashSet<RoadNode>();
	associatedLinks = new HashSet<Road>();
	this.precision = precision;
    }

    /**
     * Insert an {@link RoadNode} into this tree
     *
     * @param node
     */
    public void insert(RoadNode node) {
	if (node.inRoads.size() > 0 && node.outRoads.size() > 0) {
	    associatedNodes.add(node);
	    associatedLinks.addAll(node.getInRoads());
	    associatedLinks.addAll(node.getOutRoads());
	}
    }

    /**
     * Set all the geohash specific surge parameters to zero.
     */
    public void resetSurgeParams() {
	ignoreDax = 0;
	availableDax = 0;
	demandMet = 0;
	demandUnMet = 0;
	supplyMet = 0;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof GeoHash)
	    return this.uniqueId.equals(((GeoHash) o).uniqueId);
	else
	    return false;
    }

    @Override
    public int hashCode() {
	return this.uniqueId.hashCode();
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
    public Rectangle getBounds() {
	return bounds;
    }

    public String getUniqueId() {
	return uniqueId;
    }

    /**
     * Return the precision of this geohash.
     *
     * @return precision as an integer.
     */
    public int getPrecision() {
	return precision;
    }

    /**
     * This is a separate method that must be called at the very beginning to
     * instantiate a surge map for different taxi types if you are
     *
     * @param taxiTypes
     */
    public void instantiateSurgeMap(List<Integer> taxiTypes) {
	surgeMap = new HashMap<Integer, Double>();
	for (Integer taxiType : taxiTypes)
	    surgeMap.put(taxiType, 1.0);
    }

    /**
     * Get the met i.e. allocated pax demand in the geohash.
     *
     * @return
     */
    public double getDemandMet() {
	return demandMet;
    }

    /**
     * Set the met demand i.e allocated passengers in the geohash.
     *
     * @param demandGeoHash
     */
    public void setDemandMet(double demandGeoHash) {
	this.demandMet = demandGeoHash;
    }

    /**
     * Get the number of unallocated passenger demand in the geohash.
     *
     * @return
     */
    public double getDemandUnmet() {
	return demandUnMet;
    }

    /**
     * Set the number of unallocated passengers in the geohash.
     *
     * @param demandUnMetGeohash
     */
    public void setDemandUnMet(double demandUnMetGeohash) {
	this.demandUnMet = demandUnMetGeohash;
    }

    /**
     * Get the supply for this geohash.
     *
     * @return
     */
    public double getSupply() {
	return getSupplyUnmet() + supplyMet;
    }

    /**
     * Get the unmet supply for this geohash which takes into account available
     * dax accounting for drivers who ignore.
     *
     * @return
     */
    public double getSupplyUnmet() {
	return availableDax - ignoreDax * ignoredDiscountRate;

    }

    /**
     * Get the number of drivers who have ignored jobs in the geohash.
     *
     * @return
     */
    public double getIgnoreDax() {
	return ignoreDax;
    }

    /**
     * Set the number of ignored drivers in the geohash
     *
     * @param ignoreDax
     */
    public void setIgnoreDax(double ignoreDax) {
	this.ignoreDax = ignoreDax;
    }

    /**
     * Get the surge associated with this geohash for the taxi type id.
     *
     * @param taxiTypeId
     * @return
     */
    public double getSurge(int taxiTypeId) {
	return surgeMap.get(taxiTypeId);
    }

    /**
     * Set the surge associated with this geohash for the taxiType id
     *
     * @param surge
     *            the surge for the taxi type
     * @param taxiType
     *            the taxi type ID.
     */
    public void setSurge(double surge, int taxiType) {
	surgeMap.put(taxiType, surge);
    }

    /**
     * get the overall demand i.e. total number of pax in the geohash.
     *
     * @return
     */
    public double getDemand() {
	return (demandMet + demandUnMet);
    }

    /**
     * get the number of available drivers in the geohash.
     *
     * @return
     */
    public double getAvailableDax() {
	return availableDax;
    }

    /**
     * Set the number of available drivers in the geohash.
     *
     * @param availableDax
     */
    public void setAvailableDax(double availableDax) {
	this.availableDax = availableDax;
    }

    /**
     * Returns the the number of drivers who occupied, i.e. on call or in
     * transit in this geohash.
     *
     * @return
     */
    public double getSupplyMet() {
	return supplyMet;
    }

    /**
     * set the number of on call and in transit drivers in the geohash.
     *
     * @param supplyMet
     */
    public void setSupplyMet(double supplyMet) {
	this.supplyMet = supplyMet;
    }

}
