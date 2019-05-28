package osmdatahandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

import net.morbz.osmonaut.EntityFilter;
import net.morbz.osmonaut.IOsmonautReceiver;
import net.morbz.osmonaut.Osmonaut;
import net.morbz.osmonaut.osm.Entity;
import net.morbz.osmonaut.osm.EntityType;
import net.morbz.osmonaut.osm.Node;
import net.morbz.osmonaut.osm.Tags;
import net.morbz.osmonaut.osm.Way;
import networkmodel.Road;
import networkmodel.RoadNode;

/**
 * Parse road network from the binary files and push to the database. Credit the
 * OSMonaut project {@link https://github.com/MorbZ/OSMonaut} for the OSM
 * parser.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class OSMPBFToFile {

    private static String[] arr = { "unclassified", "primary_link", "raceway", "road", "secondary_link",
	    "tertiary_link", "tertiary", "living_street", "services", "trunk", "motorway_link", "motorway", "secondary",
	    "residential", "service", "trunk_link", "primary", "pedestrian" };

    private static Map<String, Integer> fileNameCityMapping = new HashMap<String, Integer>();
    private static String mapVersion;
    private static int nolanes = 0;

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {

	Properties properties = new Properties();

	if (args.length == 0)
	    properties.load(new FileInputStream("src/main/resources/config.properties"));
	else
	    properties.load(new FileInputStream(args[0]));

	Scanner scanner = new Scanner(System.in);
	System.out.print("Enter Map version: ");
	mapVersion = scanner.next();
	scanner.close();

	List<String> roadTypes = Arrays.asList(arr);
	fileNameCityMapping.put("SIN.osm.pbf", 6);
	fileNameCityMapping.put("CGK.osm.pbf", 10);
	fileNameCityMapping.put("MNL.osm.pbf", 4);
	fileNameCityMapping.put("SGN.osm.pbf", 9);
	fileNameCityMapping.put("BKK.osm.pbf", 5);
	fileNameCityMapping.put("KUL.osm.pbf", 1);
	fileNameCityMapping.put("SUB.osm.pbf", 18);
	fileNameCityMapping.put("BDO.osm.pbf", 28);
	fileNameCityMapping.put("KNO.osm.pbf", 35);

	File file = new File(properties.getProperty("pbf.download.path") + mapVersion + "/");
	File[] files = file.listFiles(new FilenameFilter() {

	    @Override
	    public boolean accept(File dir, String name) {
		if (name.toLowerCase().endsWith(".pbf")) {
		    return true;
		} else {
		    return false;
		}
	    }
	});

	for (File f : files) {
	    nolanes = 0;
	    int cityId = fileNameCityMapping.get(f.getName());
	    System.out.println("Begin parsing road network for city " + cityId);

	    Map<Long, RoadNode> nodesMap = new HashMap<Long, RoadNode>();
	    Map<Long, Road> roadsMap = new HashMap<>();

	    Set<String> values = new HashSet<>();

	    EntityFilter filter = new EntityFilter(true, true, false);
	    // Set the binary OSM source file
	    Osmonaut naut = new Osmonaut(properties.getProperty("pbf.download.path") + mapVersion + "/" + f.getName(),
		    filter);
	    // Start scanning by implementing the interface
	    naut.scan(new IOsmonautReceiver() {
		@Override
		public boolean needsEntity(EntityType type, Tags tags) {
		    return true;
		}

		@Override
		public void foundEntity(Entity entity) {
		    if (entity instanceof Way) {
			Way wayRoad = (Way) entity;
			if (wayRoad.getTags().hasKey("highway")
				&& roadTypes.contains(wayRoad.getTags().get("highway"))) {
			    values.add(wayRoad.getTags().get("highway"));
			    List<Node> nodes = wayRoad.getNodes();
			    long roadId = wayRoad.getId();
			    String roadType = wayRoad.getTags().get("highway");
			    int numberOfLanes = 1;
			    if (wayRoad.getTags().hasKey("lanes")
				    && wayRoad.getTags().get("lanes").matches("-?\\d+(\\.\\d+)?")) {

				Double lanes = Double.parseDouble(wayRoad.getTags().get("lanes"));
				numberOfLanes = lanes.intValue();
			    } else {
				nolanes++;
			    }

			    String oneWay = wayRoad.getTags().get("oneway");
			    boolean isOneway = false;
			    if (oneWay != null && oneWay.equalsIgnoreCase("yes"))
				isOneway = true;

			    String name = wayRoad.getTags().get("name");
			    // some cities have names which are not in English.
			    // Some times in places such as Jakarta even the
			    // english name is corrupted, can't do much here.
			    if (name == null || name.matches("[^\\x00-\\x7F]+"))
				name = wayRoad.getTags().get("name:en");

			    Road osmRoad = new Road(roadId);
			    osmRoad.setName(name);
			    osmRoad.setLaneCount(numberOfLanes);
			    osmRoad.setOneWay(isOneway);
			    osmRoad.setRoadType(roadType);

			    if (wayRoad.getTags().hasKey("tunnel")
				    && wayRoad.getTags().get("tunnel").equalsIgnoreCase("yes"))
				osmRoad.setTunnel(true);

			    if (wayRoad.getTags().hasKey("junction")
				    && wayRoad.getTags().get("junction").equalsIgnoreCase("roundabout"))
				osmRoad.setRoundabout(true);

			    for (int i = 0; i < nodes.size(); i++) {
				Node node = nodes.get(i);
				RoadNode roadNode = new RoadNode(node.getId(), node.getLatlon().getLon(),
					node.getLatlon().getLat());
				nodesMap.put(node.getId(), roadNode);
				if (i == 0)
				    osmRoad.setBeginNode(roadNode);
				if (i == nodes.size() - 1)
				    osmRoad.setEndNode(roadNode);
				osmRoad.getRoadNodes().add(roadNode);
			    }
			    roadsMap.put(roadId, osmRoad);
			}
		    }

		}

	    });

	    System.out.println(nodesMap.size() + " number of nodes in city " + cityId);
	    System.out.println(roadsMap.size() + " number of roads in city " + cityId);
	    System.out.println("number of roads with no lane info:" + nolanes);

	    // System.exit(0);

	    String dirRoadNetworkFiles = properties.getProperty("road.network.files.dir");
	    dirRoadNetworkFiles = dirRoadNetworkFiles + mapVersion + "/unnormalized";

	    File directory = new File(dirRoadNetworkFiles);
	    if (!directory.exists()) {
		Path dir = Paths.get(dirRoadNetworkFiles);
		Files.createDirectories(dir);

	    }

	    System.out.println("Writing nodes for city " + cityId + " to file.");
	    BufferedWriter bw = new BufferedWriter(
		    new FileWriter(new File(dirRoadNetworkFiles + "/nodes_" + cityId + "_" + mapVersion + ".txt")));
	    bw.write("node_id\tlongitude\tlatitude\tcity_id\tmap_version\n");
	    for (RoadNode roadNode : nodesMap.values())
		bw.write(roadNode.getNodeId() + "\t" + roadNode.getX() + "\t" + roadNode.getY() + "\t" + cityId + "\t"
			+ mapVersion + "\n");

	    bw.flush();
	    bw.close();

	    System.out.println("Writing roads for city " + cityId + " to file.");

	    bw = new BufferedWriter(
		    new FileWriter(new File(dirRoadNetworkFiles + "/roads_" + cityId + "_" + mapVersion + ".txt")));
	    bw.write(
		    "road_id\tnodes\troadname\tlanes\tis_oneway\troadtype\tcity_id\tmap_version\tis_routable\tis_tunnel\tis_roundabout\n");

	    for (Road road : roadsMap.values()) {
		StringBuffer buffer = new StringBuffer("");
		List<RoadNode> nodeIds = road.getRoadNodes();
		for (int i = 0; i < nodeIds.size(); i++) {
		    if (i == nodeIds.size() - 1) {
			buffer.append(nodeIds.get(i).getNodeId());
		    } else {
			buffer.append(nodeIds.get(i).getNodeId() + ",");
		    }
		}

		bw.write(
			road.getRoadId() + "\t" + buffer.toString() + "\t" + road.getName() + "\t" + road.getLaneCount()
				+ "\t" + road.isOneWay() + "\t" + road.getRoadType() + "\t" + cityId + "\t" + mapVersion
				+ "\t" + false + "\t" + road.isTunnel() + "\t" + road.isRoundabout() + "\n");

	    }
	    // you need to add what ever is left
	    bw.flush();
	    bw.close();

	    System.out.println("finished writing roads for city " + cityId + " to file..");

	}

    }

}
