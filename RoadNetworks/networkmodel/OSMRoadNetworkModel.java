package networkmodel;

import com.spatial4j.core.io.GeohashUtils;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.io.ParseException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.json.JSONObject;
import tech.tablesaw.api.Row;
import tech.tablesaw.api.Table;
import tech.tablesaw.io.csv.CsvReadOptions;

/**
 * Road network model for a city the OSM road network. The nodes and links are read from a database.
 *
 * @author abhinav.sunderrajan
 */
public class OSMRoadNetworkModel extends RoadNetworkModel {

  private static String[] arr = {
    "unclassified",
    "primary_link",
    "raceway",
    "road",
    "secondary_link",
    "tertiary_link",
    "tertiary",
    "living_street",
    "services",
    "trunk",
    "motorway_link",
    "motorway",
    "secondary",
    "residential",
    "service",
    "trunk_link",
    "primary",
    "pedestrian",
    "construction"
  };
  // mapping of expected speed for a road type
  private Map<String, Double> expectedSpeedMap = new HashMap<String, Double>();
  private int cityId;
  private Set<Road> bidirectionalRoads = new LinkedHashSet<Road>();
  private long maxId = 0;
  protected Map<String, PlanningArea> planningAreas;
  private boolean loadPlanningAreas;
  private boolean resolveBidirectional;
  private static final GeometryFactory gf = new GeometryFactory();

  /**
   * Road the OSM road network model in memory for a given city.
   *
   * @param dbConnectionProperties
   * @param roadDataTable
   * @param nodeDataTable
   * @param cityId
   * @param mapversion the id of the map version
   * @param geoHashPrecision the geohash precision.
   * @param loadPlanningAreas load the planning areas or not.
   * @param resolveBidirectional should we resolve the bidirectional links in to two directed links
   *     or not?
   */
  public OSMRoadNetworkModel(
      Properties dbConnectionProperties,
      String roadDataTable,
      String nodeDataTable,
      int cityId,
      String mapversion,
      int geoHashPrecision,
      boolean loadPlanningAreas,
      boolean resolveBidirectional) {
    super(dbConnectionProperties, roadDataTable, nodeDataTable, geoHashPrecision);
    try {
      this.geoHashPrecision = geoHashPrecision;
      this.cityId = cityId;
      this.loadPlanningAreas = loadPlanningAreas;
      this.resolveBidirectional = resolveBidirectional;
      loadNodesAndRoadsFromDB(mapversion);
      geoHashAssociation();
      if (loadPlanningAreas) planningAreasAssociation();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Load the road network from files
   *
   * @param roadFilePath path of file containing edges.
   * @param nodeFilePath path of file containing nodes.
   * @param cityId the city id.
   * @param geoHashPrecision geohash precision
   * @param loadPlanningAreas load the planning areas or not.
   * @param resolveBidirectional should we resolve the bidirectional links in to two directed links
   *     or not?
   */
  public OSMRoadNetworkModel(
      String roadFilePath,
      String nodeFilePath,
      int cityId,
      int geoHashPrecision,
      boolean loadPlanningAreas,
      boolean resolveBidirectional) {
    super(geoHashPrecision);
    this.geoHashPrecision = geoHashPrecision;
    this.loadPlanningAreas = loadPlanningAreas;
    this.cityId = cityId;
    this.resolveBidirectional = resolveBidirectional;
    try {
      loadNodesAndRoadsFromFile(roadFilePath, nodeFilePath);
      geoHashAssociation();
      if (loadPlanningAreas) planningAreasAssociation();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void planningAreasAssociation() throws SQLException, ParseException {
    LoadPlanningArea lpa = new LoadPlanningArea();
    planningAreas = lpa.loadAreaPolygonSG(cityId);
    allNodesMap
        .values()
        .parallelStream()
        .forEach(
            node -> {
              for (PlanningArea pa : planningAreas.values()) {
                if (pa.getPolygon().contains(gf.createPoint(node.getPosition()))) {
                  pa.addRoadsToPolygon(node.getInRoads());
                  pa.addRoadsToPolygon(node.getOutRoads());
                  pa.addNodesToPolygon(node);
                }
              }
            });
  }

  /**
   * Load the road network from file.
   *
   * @param roadFilePath
   * @param nodeFilePath
   * @throws IOException
   */
  private void loadNodesAndRoadsFromFile(String roadFilePath, String nodeFilePath)
      throws IOException {
    speedAssociation();
    // System.out.println("Loading nodes for city-id:" + cityId);

    Table nodesTable =
        Table.read().csv(CsvReadOptions.builder(nodeFilePath).separator('\t').build());
    for (Row row : nodesTable) {
      RoadNode node =
          new RoadNode(row.getLong("node_id"), row.getFloat("longitude"), row.getFloat("latitude"));
      allNodesMap.put(node.getNodeId(), node);
    }

    BufferedReader br = new BufferedReader(new FileReader(new File(roadFilePath)));
    boolean hasTunnelInfo = false;
    // check if OSM-ID exists.
    while (br.ready()) {
      String line = br.readLine();
      if (line.contains("road_id")) {
        if (line.contains("is_tunnel")) hasTunnelInfo = true;
        continue;
      }

      String splitLine[] = line.split("\t");
      String nodeList = splitLine[1];
      String[] split = nodeList.replaceAll("^\"|\"$", "").split(",");

      // cannot have one node roads
      if (split.length == 1) continue;

      String roadType = splitLine[5].replaceAll("^\"|\"$", "");

      Long roadId = Long.parseLong(splitLine[0]);
      if (roadId > maxId) maxId = roadId;

      boolean isOneWay = splitLine[4].equalsIgnoreCase("TRUE") ? true : false;
      Road road = new Road(roadId);
      road.setOneWay(isOneWay);
      if (hasTunnelInfo) road.setTunnel(splitLine[9].equalsIgnoreCase("TRUE") ? true : false);
      else road.setTunnel(false);

      road.setLaneCount(Integer.parseInt(splitLine[3]));

      road.setRoadType(roadType);
      double expectedSpeed = 2.1;
      // road has 4 kinds of speeds mean/expected, median, upper, lower.
      if (expectedSpeedMap.containsKey(roadType)) expectedSpeed = expectedSpeedMap.get(roadType);

      road.setExpectedSpeed(expectedSpeed);
      road.setMedianSpeed(expectedSpeed);
      road.setUpperBoundSpeed(expectedSpeed);
      road.setLowerBoundSpeed(expectedSpeed);

      String name = splitLine[2].replaceAll("^\"|\"$", "");
      name = name.equals("null") ? null : name;
      road.setName(name);

      setRoadNodes(road, split);
      allRoadsMap.put(roadId, road);

      if (!isOneWay) bidirectionalRoads.add(road);
    }
    br.close();

    if (resolveBidirectional) resolveBidirectionalEdges();
  }

  /** Create duplicate roads for the bidirectional roads. */
  private void resolveBidirectionalEdges() {

    // one way roads cannot be one way
    for (Road road : allRoadsMap.values()) {
      if (road.isOneWay() && road.getEndNode().outRoads.size() == 0) bidirectionalRoads.add(road);
    }

    for (Road bidirectionalRoad : bidirectionalRoads) {

      bidirectionalRoad.setOneWay(true);
      long roadId = ++maxId;
      Road newRoad = new Road(roadId);
      newRoad.setLaneCount(bidirectionalRoad.getLaneCount());

      newRoad.setRoadType(bidirectionalRoad.getRoadType());
      newRoad.setOneWay(true);
      newRoad.setName(bidirectionalRoad.getName());

      // road has 4 kinds of speeds mean/expected, median, upper, lower.
      newRoad.setExpectedSpeed(expectedSpeedMap.get(bidirectionalRoad.getRoadType()));
      newRoad.setMedianSpeed(expectedSpeedMap.get(bidirectionalRoad.getRoadType()));
      newRoad.setUpperBoundSpeed(expectedSpeedMap.get(bidirectionalRoad.getRoadType()));
      newRoad.setLowerBoundSpeed(expectedSpeedMap.get(bidirectionalRoad.getRoadType()));

      List<RoadNode> roadNodes = new ArrayList<>();
      // A reversal of the original
      for (int i = bidirectionalRoad.getRoadNodes().size() - 1; i >= 0; i--) {
        RoadNode node = bidirectionalRoad.getRoadNodes().get(i);
        if (i == bidirectionalRoad.getRoadNodes().size() - 1) {
          newRoad.setBeginNode(node);
          node.getOutRoads().add(newRoad);
          node.setBeginOrEnd(true);
        } else if (i == 0) {
          newRoad.setEndNode(node);
          node.getInRoads().add(newRoad);
          node.setBeginOrEnd(true);
        } else {
          node.getInRoads().add(newRoad);
          node.getOutRoads().add(newRoad);
        }
        roadNodes.add(node);
      }

      newRoad.setRoadNodes(roadNodes);
      allRoadsMap.put(roadId, newRoad);
    }
  }

  private void speedAssociation() {

    Map<Integer, String> cityCodes = new HashMap<>();
    cityCodes.put(6, "SIN_4W");
    cityCodes.put(10, "CGK_4W");
    cityCodes.put(4, "MNL_4W");
    cityCodes.put(5, "BKK_4W");

    try {

      BufferedReader br =
          new BufferedReader(new FileReader("src/main/resources/default_speeds.csv"));
      String cityCode = "SIN_4W";
      if (cityCodes.containsKey(cityId)) cityCode = cityCodes.get(cityId);

      while (br.ready()) {
        String line = br.readLine();
        if (line.contains("city_id") || !line.contains(cityCode)) continue;
        line = line.replaceAll("\"", "");
        line = line.replace(cityCode + "," + cityId + ",{", "{");
        JSONObject object = new JSONObject(line);
        for (String roadType : object.keySet())
          expectedSpeedMap.put(roadType, object.getDouble(roadType));
      }
      br.close();
      System.out.println("Default speed for city " + cityCode);
      System.out.println(expectedSpeedMap);

    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  private void geoHashAssociation() {
    for (RoadNode node : allNodesMap.values()) {
      String geoHashString = GeohashUtils.encodeLatLon(node.getY(), node.getX(), geoHashPrecision);
      GeoHash geohash = null;
      if (geoHashMap.containsKey(geoHashString)) {
        geohash = geoHashMap.get(geoHashString);
      } else {
        geohash = new GeoHash(geoHashString, geoHashPrecision);
        geoHashMap.put(geoHashString, geohash);
      }

      geohash.insert(node);
    }
  }

  /** The reset speeds method is called when you want to load new speed profiles. */
  public void resetSpeeds() {
    for (Road road : allRoadsMap.values()) {
      road.setHasSpeedInfo(false);
      String roadType = road.getRoadType();
      double expectedSpeed = 2.1;
      // road has 4 kinds of speeds mean/expected, median, upper, lower.
      if (expectedSpeedMap.containsKey(roadType)) expectedSpeed = expectedSpeedMap.get(roadType);

      road.setExpectedSpeed(expectedSpeed);
      road.setMedianSpeed(expectedSpeed);
      road.setLowerBoundSpeed(expectedSpeed);
      road.setUpperBoundSpeed(expectedSpeed);
    }
  }

  /**
   * Loading road network from the database. Note that this version has been corrected for
   * bidirectional edges.
   *
   * @param mapversion the map version to load
   */
  protected void loadNodesAndRoadsFromDB(String mapversion) throws SQLException {
    speedAssociation();
    System.out.println("Loading nodes for city-id:" + cityId);

    ResultSet rs =
        access.retrieveQueryResult(
            "SELECT * FROM "
                + nodeDataTable
                + " WHERE city_id="
                + cityId
                + " and map_version='"
                + mapversion
                + "'");
    while (rs.next()) {
      RoadNode node =
          new RoadNode(rs.getLong(1), rs.getDouble("longitude"), rs.getDouble("latitude"));
      allNodesMap.put(node.getNodeId(), node);
    }

    rs.close();

    System.out.println("Loading roads:" + cityId);
    rs =
        access.retrieveQueryResult(
            "SELECT * FROM "
                + roadDataTable
                + " WHERE city_id="
                + cityId
                + " and map_version='"
                + mapversion
                + "' order by road_id");

    while (rs.next()) {
      String nodeList = rs.getString("nodes");
      String[] split = nodeList.split(",");
      // cannot have one node roads
      if (split.length == 1) continue;
      Long roadId = rs.getLong("road_id");
      if (roadId > maxId) maxId = roadId;

      boolean isOneWay = rs.getBoolean("oneway");

      Road road = new Road(roadId);
      road.setLaneCount(rs.getInt("lanes"));
      String roadType = rs.getString("roadtype");
      road.setRoadType(roadType);
      // road has 4 kinds of speeds mean/expected, median, upper, lower.
      road.setExpectedSpeed(expectedSpeedMap.get(roadType));
      road.setMedianSpeed(expectedSpeedMap.get(roadType));
      road.setUpperBoundSpeed(expectedSpeedMap.get(roadType));
      road.setLowerBoundSpeed(expectedSpeedMap.get(roadType));
      if (resolveBidirectional) road.setOneWay(true);
      else road.setOneWay(isOneWay);

      road.setName(rs.getString("roadname"));

      setRoadNodes(road, split);

      allRoadsMap.put(roadId, road);
      if (!isOneWay) bidirectionalRoads.add(road);
    }

    rs.close();
    if (resolveBidirectional) resolveBidirectionalEdges();
  }

  /**
   * Associate the road with its constituent nodes
   *
   * @param road the road object
   * @param split the constituent node IDs.
   */
  private void setRoadNodes(Road road, String[] split) {
    List<RoadNode> roadNodes = new ArrayList<>();

    for (int i = 0; i < split.length; i++) {
      long nodeId = Long.parseLong(split[i].trim());
      RoadNode node = allNodesMap.get(nodeId);
      if (i == 0) {
        beginAndEndNodes.add(node);
        road.setBeginNode(node);
        node.getOutRoads().add(road);
        // if we do not resolve bidirectional roads
        if (!road.isOneWay()) node.getInRoads().add(road);
        node.setBeginOrEnd(true);
      } else if (i == split.length - 1) {
        beginAndEndNodes.add(node);
        road.setEndNode(node);
        node.getInRoads().add(road);
        // if we do not resolve bidirectional roads
        if (!road.isOneWay()) node.getOutRoads().add(road);
        node.setBeginOrEnd(true);
      } else {
        node.getInRoads().add(road);
        if (road.isOneWay()) node.getOutRoads().add(road);
        node.getOutRoads().add(road);
      }
      roadNodes.add(node);
    }
    road.setRoadNodes(roadNodes);
  }

  /**
   * Pass the path to the CSV file which contains intersection node IDS.
   *
   * @param pathToCSVFile
   * @throws IOException
   */
  public void tagTrafficLightNodes(String pathToCSVFile) throws IOException {
    Table junctionNodeDF = Table.read().csv(pathToCSVFile);
    for (Row row : junctionNodeDF) allNodesMap.get(row.getLong("node_id")).setTrafficLight(true);
  }

  /**
   * Retrieves the planning areas for the city.
   *
   * @return
   */
  public Map<String, PlanningArea> getPlanningAreas() {
    return planningAreas;
  }

  /**
   * If the planning areas are loaded or not.
   *
   * @return
   */
  public boolean isLoadPlanningAreas() {
    return loadPlanningAreas;
  }

  /**
   * Get the expected speeds on a road type basis.
   *
   * @return
   */
  public Map<String, Double> getExpectedSpeedMap() {
    return expectedSpeedMap;
  }
}
