package osmdatahandler;

import com.vividsolutions.jts.geom.Coordinate;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import networkmodel.Road;
import networkmodel.RoadNode;
import networkutils.DatabaseAccess;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * Parse the line geojson file containing the road network information for a city and add to
 * database. This is deprecated we will not be using this class any more. Retaining it for future
 * reference if required.
 *
 * @author abhinav.sunderrajan
 */
@Deprecated
public class OSMGeoJSONToDB {
  private static final String FILE_LOCATION = "path";
  private static Set<String> roadTypes = new HashSet<>();
  private static Set<Road> roads = new HashSet<Road>();
  private static Map<Coordinate, RoadNode> nodesMap = new HashMap<Coordinate, RoadNode>();
  private static int nodeId = 2244249;
  private static int roadId = 3503524;
  private static final int CITY_ID = 6;

  public static void main(String[] args) {

    try {

      String[] arr = {
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
        "pedestrian"
      };
      for (int i = 0; i < arr.length; i++) {
        roadTypes.add(arr[i]);
      }

      File file = new File(FILE_LOCATION);
      JSONTokener tokener = new JSONTokener(new FileReader(file));
      JSONObject jsonobj = new JSONObject(tokener);

      JSONArray array = jsonobj.getJSONArray("features");

      Iterator<Object> it = array.iterator();
      while (it.hasNext()) {
        JSONObject feature = (JSONObject) it.next();
        JSONObject properties = feature.getJSONObject("properties");
        if (!properties.isNull("highway")) {
          String roadType = properties.getString("highway");
          if (!roadTypes.contains(roadType)) {
            continue;
          }

          String osm_id = properties.getString("uid");
          System.out.println(roadType + " " + Long.parseLong(osm_id) + " " + osm_id);

          JSONObject geometry = feature.getJSONObject("geometry");
          if (geometry.getString("type").equalsIgnoreCase("LineString")) {
            JSONArray coordinates = geometry.getJSONArray("coordinates");
            List<RoadNode> roadNodes = new ArrayList<RoadNode>();
            for (int i = 0; i < coordinates.length(); i++) {
              JSONArray coordinate = coordinates.getJSONArray(i);
              double lon = coordinate.getDouble(0);
              double lat = coordinate.getDouble(1);

              Coordinate coord = new Coordinate(lon, lat);

              if (nodesMap.containsKey(coord)) {
                roadNodes.add(nodesMap.get(coord));
              } else {
                RoadNode node = new RoadNode(++nodeId, lon, lat);
                nodesMap.put(coord, node);
                roadNodes.add(node);
              }
            }

            Road road = new Road(++roadId);
            road.setRoadNodes(roadNodes);
            road.setRoadType(roadType);

            if (!properties.isNull("oneway")) {
              String oneWay = (String) properties.get("oneway");
              if (oneWay == null) {
                road.setOneWay(false);
              } else {
                if (oneWay.equalsIgnoreCase("yes")) {
                  road.setOneWay(true);
                } else {
                  road.setOneWay(false);
                }
              }
            }
            if (!properties.isNull("name")) {
              String name = properties.getString("name");
              if (name == null) {
                road.setName("");
              } else {
                name = name.replace("'", "");
                road.setName(name);
              }

            } else {
              road.setName("");
            }

            if (!properties.isNull("lanes")) {
              String s = properties.getString("lanes");
              if (s.matches("[-+]?\\d*\\.?\\d+")) road.setLaneCount(Integer.parseInt(s));
              else road.setLaneCount(-1);
            } else {
              road.setLaneCount(-1);
            }

            roads.add(road);
          }
        }
      }

      System.out.println("Number of nodes in city " + CITY_ID + ":" + nodesMap.size());
      System.out.println("Number of roads in city " + CITY_ID + ":" + roads.size());

      System.exit(0);
      System.out.println("Inserting nodes into database");
      Properties connectionProperties = new Properties();
      connectionProperties.load(new FileInputStream("src/main/resources/connection.properties"));
      DatabaseAccess access = new DatabaseAccess(connectionProperties);

      for (Entry<Coordinate, RoadNode> entry : nodesMap.entrySet()) {
        RoadNode node = entry.getValue();
        access.executeUpdate(
            "INSERT INTO openstreetmap.nodes VALUES ("
                + node.getNodeId()
                + ","
                + node.getX()
                + ","
                + node.getY()
                + ","
                + CITY_ID
                + ")");
      }

      System.out.println("Inserting edges into database");
      for (Road road : roads) {
        StringBuffer buffer = new StringBuffer("");
        List<RoadNode> nodeIds = road.getRoadNodes();
        for (int i = 0; i < nodeIds.size(); i++) {
          if (i == nodeIds.size() - 1) {
            buffer.append(nodeIds.get(i).getNodeId());
          } else {
            buffer.append(nodeIds.get(i).getNodeId() + ",");
          }
        }

        access.executeUpdate(
            "INSERT INTO openstreetmap.roads (road_id,nodes,roadname,lanes,oneway,roadtype,city_id) VALUES("
                + road.getRoadId()
                + ",'"
                + buffer.toString()
                + "','"
                + road.getName()
                + "',"
                + road.getLaneCount()
                + ","
                + road.isOneWay()
                + ",'"
                + road.getRoadType()
                + "' ,"
                + CITY_ID
                + ")");
      }

      System.out.println("Finished..");
      access.closeConnection();

    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
