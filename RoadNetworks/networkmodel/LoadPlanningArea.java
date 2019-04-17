package networkmodel;

import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import networkutils.DatabaseAccess;
import networkutils.GeoFunctions;

/**
 * Load the planning areas from the relevant table in the database. Note that I have added a buffer
 * of 100 meters around the polygon provided by presto.
 *
 * @author abhinav.sunderrajan
 */
public class LoadPlanningArea {
  private DatabaseAccess access;
  private Map<String, PlanningArea> areaPolygon;
  public static final WKTReader wkt = new WKTReader();
  private static final String DRIVER = "com.facebook.presto.jdbc.PrestoDriver";
  private static final double BUFFER = 100.0;

  public LoadPlanningArea() {
    try {
      Properties connectionProperties = new Properties();
      connectionProperties.load(new FileInputStream("src/main/resources/connection.properties"));
      access = new DatabaseAccess(connectionProperties, DRIVER);
      areaPolygon = new TreeMap<String, PlanningArea>();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Load the planning areas for the city id parameter.
   *
   * @param cityId
   * @return
   * @throws SQLException
   * @throws ParseException
   */
  public Map<String, PlanningArea> loadAreaPolygonSG(int cityId)
      throws SQLException, ParseException {
    String query =
        "select * from table_name where city_id=" + cityId + " order by area, pointorder";

    System.out.println("Retrieving the area polygons in city " + cityId);
    ResultSet rs = getAccess().retrieveQueryResult(query);

    String prev = "";
    StringBuffer buffer = new StringBuffer("POLYGON ((");
    String firstLatLon = null;
    String latLon = null;
    while (rs.next()) {
      String area = rs.getString("area");
      double latitude = rs.getDouble("latitude");
      double longitude = rs.getDouble("longitude");

      if (!prev.equals(area)) {
        if (!prev.equalsIgnoreCase("")) {

          // Check if the polygon is closed.
          if (!latLon.equalsIgnoreCase(firstLatLon)) {
            buffer.append(firstLatLon + "))");
          } else {
            buffer.deleteCharAt(buffer.length() - 1);
            buffer.append("))");
          }

          Polygon polygon = (Polygon) wkt.read(buffer.toString());
          double k = GeoFunctions.metersToDecimalDegrees(BUFFER, polygon.getCentroid().getY());
          polygon = (Polygon) polygon.buffer(k);
          areaPolygon.put(prev, new PlanningArea(polygon, prev));
          buffer = new StringBuffer("POLYGON ((");
          firstLatLon = null;
        }
      }

      latLon = longitude + " " + latitude;
      if (firstLatLon == null) firstLatLon = latLon;

      buffer.append(latLon + ",");
      prev = area;
    }

    // for the final area
    // Check if the polygon is closed.
    if (!latLon.equalsIgnoreCase(firstLatLon)) {
      buffer.append("," + firstLatLon + "))");
    } else {
      buffer.deleteCharAt(buffer.length() - 1);
      buffer.append("))");
    }

    Polygon polygon = (Polygon) wkt.read(buffer.toString());
    double k = GeoFunctions.metersToDecimalDegrees(BUFFER, polygon.getCentroid().getY());
    polygon = (Polygon) polygon.buffer(k);
    areaPolygon.put(prev, new PlanningArea(polygon, prev));
    System.out.println("Number of areas in city ID " + cityId + " is:" + areaPolygon.size());
    return areaPolygon;
  }

  /**
   * Get the database access object.
   *
   * @return
   */
  public DatabaseAccess getAccess() {
    return access;
  }
}
