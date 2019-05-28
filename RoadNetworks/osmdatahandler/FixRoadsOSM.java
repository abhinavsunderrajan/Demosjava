package osmdatahandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.vividsolutions.jts.io.ParseException;

import networkmodel.OSMRoadNetworkModel;
import networkmodel.Road;
import networkmodel.RoadNode;

/**
 * Post process the road network for normalization.
 * 
 * 
 * @author abhinav.sunderrajan
 * 
 */
public class FixRoadsOSM {

    private OSMRoadNetworkModel roadNetworkModel;
    private static int cityId;
    private static String mapVersion;

    /**
     * Fix the code here. This needs to be done recursively.
     * 
     * 
     * @return
     * @throws SQLException
     */
    public void splitRoadsAtIntersections() throws SQLException {

	long maxRoadId = roadNetworkModel.getAllRoadsMap().values().stream().mapToLong(road -> road.getRoadId()).max()
		.getAsLong();

	// For beginning and end nodes.
	Set<Road> newRoads = new HashSet<>();

	for (Road splitRoad : roadNetworkModel.getAllRoadsMap().values()) {
	    List<Integer> splits = new ArrayList<>();
	    for (int i = 1; i < splitRoad.getRoadNodes().size() - 1; i++)
		if (roadNetworkModel.getBeginAndEndNodes().contains(splitRoad.getRoadNodes().get(i)))
		    splits.add(i);

	    if (splits.size() > 0) {
		ArrayList<RoadNode> nodesListArray[] = new ArrayList[splits.size() + 1];
		// initializing
		for (int i = 0; i < splits.size() + 1; i++)
		    nodesListArray[i] = new ArrayList<RoadNode>();

		int index = 0;
		int j = 0;

		for (int i = 0; i < splitRoad.getRoadNodes().size(); i++) {
		    if (index == splits.size()) {
			nodesListArray[j].add(splitRoad.getRoadNodes().get(i));
		    } else {
			if (i == splits.get(index)) {
			    nodesListArray[j].add(splitRoad.getRoadNodes().get(i));
			    j++;
			    index++;
			}
			nodesListArray[j].add(splitRoad.getRoadNodes().get(i));
		    }

		}

		for (int i = 0; i < nodesListArray.length; i++) {
		    if (i == 0) {
			splitRoad.setRoadNodes(nodesListArray[i]);
		    } else {

			String roadName = splitRoad.getName() == null ? null : splitRoad.getName().replace("'", "''");
			long roadId = ++maxRoadId;
			Road newRoad = new Road(roadId);
			newRoad.setLaneCount(splitRoad.getLaneCount());

			newRoad.setRoadType(splitRoad.getRoadType());
			newRoad.setOneWay(splitRoad.isOneWay());
			newRoad.setName(roadName);

			newRoad.setRoadNodes(nodesListArray[i]);
			newRoad.setTunnel(splitRoad.isTunnel());
			newRoad.setRoundabout(splitRoad.isRoundabout());
			newRoads.add(newRoad);

		    }

		}

	    }

	}

	System.out.println("Created " + newRoads.size() + " new split roads");
	for (Road newRoad : newRoads)
	    roadNetworkModel.getAllRoadsMap().put(newRoad.getRoadId(), newRoad);

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

	for (Road road : roadNetworkModel.getAllRoadsMap().values()) {
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

		roadNetworkModel.getAllRoadsMap().remove(outRoad.getRoadId());
		++mergedCount;
	    }
	}

	return mergedCount;
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ParseException {
	mapVersion = args[0];
	cityId = Integer.parseInt(args[1]);
	if (mapVersion == "" || mapVersion == null)
	    throw new IllegalArgumentException("Enter a correct map version to proceed");

	Properties properties = new Properties();
	properties.load(new FileInputStream("src/main/resources/config.properties"));

	String dirRoadNetworkFiles = properties.getProperty("road.network.files.dir");

	FixRoadsOSM clean = new FixRoadsOSM();
	clean.roadNetworkModel = new OSMRoadNetworkModel(
		dirRoadNetworkFiles + mapVersion + "/unnormalized/" + "roads_" + cityId + "_" + mapVersion + ".txt",
		dirRoadNetworkFiles + mapVersion + "/unnormalized/" + "nodes_" + cityId + "_" + mapVersion + ".txt",
		cityId, 5, false, false);

	System.out.println("Splitting roads at intersections");
	clean.splitRoadsAtIntersections();
	System.out.println("Finished splitting roads at intersections");

	System.out.println("write normalized road network to file..");

	dirRoadNetworkFiles = dirRoadNetworkFiles + mapVersion + "/normalized";

	File directory = new File(dirRoadNetworkFiles);
	if (!directory.exists()) {
	    Path dir = Paths.get(dirRoadNetworkFiles);
	    Files.createDirectories(dir);
	}

	System.out.println("Writing normalized roads for city " + cityId + " to file.");

	BufferedWriter bw = new BufferedWriter(
		new FileWriter(new File(dirRoadNetworkFiles + "/roads_" + cityId + "_" + mapVersion + ".txt")));
	bw.write(
		"road_id\tnodes\troadname\tlanes\tis_oneway\troadtype\tcity_id\tmap_version\tis_routable\tis_tunnel\tis_roundabout\n");

	for (Road road : clean.roadNetworkModel.getAllRoadsMap().values()) {
	    StringBuffer buffer = new StringBuffer("");
	    List<RoadNode> nodeIds = road.getRoadNodes();
	    for (int i = 0; i < nodeIds.size(); i++) {
		if (i == nodeIds.size() - 1) {
		    buffer.append(nodeIds.get(i).getNodeId());
		} else {
		    buffer.append(nodeIds.get(i).getNodeId() + ",");
		}
	    }

	    bw.write(road.getRoadId() + "\t" + buffer.toString() + "\t" + road.getName() + "\t" + road.getLaneCount()
		    + "\t" + road.isOneWay() + "\t" + road.getRoadType() + "\t" + cityId + "\t" + mapVersion + "\t"
		    + false + "\t" + road.isTunnel() + "\t" + road.isRoundabout() + "\n");

	}
	// you need to add what ever is left
	bw.flush();
	bw.close();

	System.out.println("finished writing roads for city " + cityId + " to file..");

    }

}
