package osmdatahandler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.vividsolutions.jts.io.ParseException;

import networkmodel.Road;
import networkmodel.RoadNode;
import networkutils.DatabaseAccess;

/**
 * Post process the road network for normalization.
 * 
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class FixRoadsOSM {

    private Map<Long, RoadNode> allNodes = new HashMap<Long, RoadNode>();
    private Set<Road> cityRoads = new HashSet<Road>();
    private Set<RoadNode> beginAndEndNodes = new HashSet<RoadNode>();
    private static DatabaseAccess access;
    private String roadDataTableName;
    private String nodeDataTableName;
    private static int cityId;
    private static String mapVersion;

    /**
     * 
     * @param dbConnectionProperties
     */
    private FixRoadsOSM(Properties dbConnectionProperties, String roadDataTableName, String nodeDataTableName) {
	access = new DatabaseAccess(dbConnectionProperties);
	this.roadDataTableName = roadDataTableName;
	this.nodeDataTableName = nodeDataTableName;

    }

    private void loadNodesAndRoads() throws SQLException, FileNotFoundException, IOException, ParseException {

	allNodes.clear();
	cityRoads.clear();
	beginAndEndNodes.clear();

	ResultSet rs = access.retrieveQueryResult("SELECT node_id,longitude,latitude FROM " + nodeDataTableName
		+ " where city_id=" + cityId + " and map_version='" + mapVersion + "'");
	while (rs.next()) {
	    RoadNode node = new RoadNode(rs.getLong("node_id"), rs.getDouble("longitude"), rs.getDouble("latitude"));
	    allNodes.put(node.getNodeId(), node);

	}

	rs.close();

	rs = access.retrieveQueryResult("SELECT road_id,nodes,roadname,lanes,oneway,roadtype,is_tunnel FROM "
		+ roadDataTableName + " where city_id=" + cityId + " and map_version='" + mapVersion + "'");
	while (rs.next()) {
	    Long roadId = rs.getLong("road_id");
	    String nodeList = rs.getString("nodes");
	    String[] split = nodeList.split(",");
	    List<RoadNode> roadNodes = new ArrayList<>();

	    Road road = new Road(roadId);
	    road.setLaneCount(rs.getInt("lanes"));
	    road.setRoadType(rs.getString("roadtype"));
	    road.setOneWay(rs.getBoolean("oneway"));
	    road.setName(rs.getString("roadname"));
	    road.setTunnel(rs.getBoolean("is_tunnel"));

	    for (int i = 0; i < split.length; i++) {
		Long nodeId = Long.parseLong(split[i]);
		RoadNode node = allNodes.get(nodeId);
		if (i == 0) {
		    beginAndEndNodes.add(node);
		    road.setBeginNode(node);
		    node.getOutRoads().add(road);
		    if (!road.isOneWay()) {
			node.getInRoads().add(road);
		    }
		} else if ((i == split.length - 1)) {
		    beginAndEndNodes.add(node);
		    road.setEndNode(node);
		    node.getInRoads().add(road);
		    if (!road.isOneWay()) {
			node.getOutRoads().add(road);
		    }

		} else {
		    node.getInRoads().add(road);
		    node.getOutRoads().add(road);
		    node.setBeginOrEnd(false);
		}
		roadNodes.add(allNodes.get(nodeId));
	    }

	    road.setRoadNodes(roadNodes);
	    cityRoads.add(road);
	}
    }

    /**
     * Fix the code here. This needs to be done recursively.
     * 
     * @return
     * @throws SQLException
     */
    public void splitRoadsAtIntersections() throws SQLException {

	ResultSet rs = access.retrieveQueryResult("select max(road_id) AS maxRoadId from " + roadDataTableName
		+ " where city_id=" + cityId + " and map_version='" + mapVersion + "'");
	long maxRoadId = 0;
	while (rs.next())
	    maxRoadId = rs.getLong("maxRoadId");

	Statement statement = access.getConnect().createStatement();

	// For beginning and end nodes.
	int roadNum = 0;
	for (Road splitRoad : cityRoads) {
	    List<Integer> splits = new ArrayList<>();
	    for (int i = 1; i < splitRoad.getRoadNodes().size() - 1; i++) {
		if (beginAndEndNodes.contains(splitRoad.getRoadNodes().get(i)))
		    splits.add(i);
	    }

	    if (splits.size() > 0) {
		++roadNum;
		if (roadNum % 1000 == 0) {
		    statement.executeBatch();
		    statement.close();
		    statement = access.getConnect().createStatement();
		    System.out.println(
			    roadNum + "\t" + splitRoad.getRoadId() + "\t" + splitRoad.getRoadNodes() + "\t" + splits);
		}

		StringBuffer[] buffers = new StringBuffer[splits.size() + 1];
		for (int i = 0; i < buffers.length; i++)
		    buffers[i] = new StringBuffer("");

		int index = 0;
		int j = 0;

		for (int i = 0; i < splitRoad.getRoadNodes().size(); i++) {

		    if (index == splits.size()) {
			buffers[j].append(splitRoad.getRoadNodes().get(i).getNodeId() + ",");
		    } else {
			if (i == splits.get(index)) {
			    buffers[j].append(splitRoad.getRoadNodes().get(i).getNodeId());
			    j++;
			    index++;
			}
			buffers[j].append(splitRoad.getRoadNodes().get(i).getNodeId() + ",");
		    }

		}

		buffers[j].deleteCharAt(buffers[j].length() - 1);

		for (int i = 0; i < buffers.length; i++) {
		    if (i == 0) {
			statement.addBatch("UPDATE " + roadDataTableName + " SET nodes='" + buffers[i].toString()
				+ "' WHERE road_id = " + splitRoad.getRoadId() + " and city_id=" + cityId
				+ " and map_version='" + mapVersion + "'");
		    } else {

			String roadName = splitRoad.getName() == null ? null : splitRoad.getName().replace("'", "''");
			String query = "INSERT INTO " + roadDataTableName
				+ " (road_id,nodes,roadname,lanes,oneway,roadtype,city_id,map_version,is_routable,is_tunnel) VALUES("
				+ ++maxRoadId + ",'" + buffers[i].toString() + "','" + roadName + "',"
				+ splitRoad.getLaneCount() + "," + splitRoad.isOneWay() + ",'" + splitRoad.getRoadType()
				+ "'," + cityId + ", '" + mapVersion + "',true," + splitRoad.isTunnel() + ")";

			statement.addBatch(query);
		    }

		}

	    }

	}

	statement.executeBatch();
	statement.close();

    }

    /**
     * Merge those which are have exactly one incoming and one outgoing at their end
     * and begin nodes respectively.
     * 
     * @throws SQLException
     */
    private int mergeRoads() throws SQLException {
	Set<Road> ignore = new HashSet<>();
	int mergedCount = 0;
	Statement statement = access.getConnect().createStatement();

	for (Road road : cityRoads) {
	    if (ignore.contains(road))
		continue;

	    RoadNode endNode = road.getEndNode();
	    if (endNode == null) {
		System.err.println(road + ", " + road.getRoadNodes());
		continue;
	    }

	    if (endNode.getInRoads().size() == 1 && endNode.getOutRoads().size() == 1) {
		Road outRoad = null;
		for (Road out : endNode.getOutRoads())
		    outRoad = out;

		if (road.equals(outRoad))
		    continue;

		int i = 0;
		for (RoadNode node : outRoad.getRoadNodes()) {
		    if (i != 0)
			road.getRoadNodes().add(node);
		    i++;
		}

		StringBuffer buffer = new StringBuffer("");
		for (int x = 0; x < road.getRoadNodes().size(); x++) {
		    if (x == road.getRoadNodes().size() - 1)
			buffer.append(road.getRoadNodes().get(x).getNodeId() + "");
		    else
			buffer.append(road.getRoadNodes().get(x).getNodeId() + ",");
		}

		// delete out road
		statement.addBatch("DELETE FROM " + roadDataTableName + " where road_id=" + outRoad.getRoadId()
			+ " and city_id=" + cityId + " and map_version='" + mapVersion + "'");

		// update the merged road in the database.
		statement.addBatch("UPDATE " + roadDataTableName + " SET nodes='" + buffer.toString()
			+ "' WHERE road_id=" + road.getRoadId() + " and city_id=" + cityId + " and map_version='"
			+ mapVersion + "'");

		ignore.add(outRoad);
		++mergedCount;
		if (mergedCount % 1000 == 0) {
		    statement.executeBatch();
		    statement.close();
		    statement = access.getConnect().createStatement();
		}

	    }

	}
	statement.executeBatch();
	statement.close();

	return mergedCount;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ParseException {
	mapVersion = args[0];
	cityId = Integer.parseInt(args[1]);
	if (mapVersion == "" || mapVersion == null)
	    throw new IllegalArgumentException("Enter a correct map version to proceed");

	System.out.println("loading roads and nodes for city " + cityId + " version " + mapVersion);

	Properties dbConnectionProperties = new Properties();
	dbConnectionProperties.load(new FileInputStream("src/main/resources/connectionAWS.properties"));
	FixRoadsOSM clean = new FixRoadsOSM(dbConnectionProperties, "osm_pbf.roads", "osm_pbf.nodes");
	/*
	 * clean.loadNodesAndRoads();
	 * 
	 * System.out.println("finished loading ");
	 * 
	 * // just assigning a random number for the loop to begin
	 * 
	 * System.out.println("merging roads"); int mergedCount = 1000; do { mergedCount
	 * = clean.mergeRoads(); clean.loadNodesAndRoads();
	 * System.out.println("finished merging roads " + mergedCount + " roads"); }
	 * while (mergedCount > 0);
	 */

	System.out.println("Splitting roads at intersections");
	clean.loadNodesAndRoads();
	clean.splitRoadsAtIntersections();
	System.out.println("Finished splitting roads at intersections");

	access.executeUpdate("UPDATE " + clean.roadDataTableName + " SET is_routable=true" + " where city_id=" + cityId
		+ " and map_version='" + mapVersion + "'");

	access.closeConnection();

    }

}
