package networkmodel;

import com.vividsolutions.jts.geom.Polygon;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents the planning area of city. The planning areas are retrieved from database.
 *
 * @author abhinav.sunderrajan
 */
public class PlanningArea {

  private Polygon polygon;
  private Set<Road> associatedRoads;
  private String areaName;
  private Set<RoadNode> associatedNodes;

  /**
   * Create a planning area from the
   *
   * @param polygon
   */
  public PlanningArea(Polygon polygon, String name) {
    this.polygon = polygon;
    this.associatedRoads = new HashSet<>();
    this.associatedNodes = new HashSet<>();
    this.areaName = name;
  }

  public Polygon getPolygon() {
    return polygon;
  }

  public Set<Road> getAssociatedRoads() {
    return associatedRoads;
  }

  public Set<RoadNode> getAssociatedNodes() {
    return associatedNodes;
  }

  public void addRoadsToPolygon(Collection<Road> roads) {
    associatedRoads.addAll(roads);
  }

  public void addNodesToPolygon(RoadNode nodes) {
    associatedNodes.add(nodes);
  }

  /**
   * Get the name of the panning area.
   *
   * @return
   */
  public String getAreaName() {
    return areaName;
  }
}
