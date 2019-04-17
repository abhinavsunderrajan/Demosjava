package route;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import org.joda.time.DateTime;
import org.json.JSONObject;

/**
 * The class which represents a trajectory.
 *
 * @author abhinav.sunderrajan
 */
public class BookingBean {

  private String trajectoryCOde;
  private Coordinate pickupLocation;
  private Coordinate dropOffLocation;
  private DateTime pickupTime;
  private DateTime dropOffTime;
  private Long driverId;
  private double distance;
  private LinkedHashSet<RouteBean> route;
  private List<JSONObject> gpsDataPoints;
  private int hourOfDayLocal;
  private double averageSpeedOfTrip;

  public BookingBean(
      String trajectoryCOde,
      Coordinate pickupLocation,
      Coordinate dropOffLocation,
      double distance,
      DateTime pickupTime,
      DateTime dropOffTime,
      Long driverId) {
    this.trajectoryCOde = trajectoryCOde;
    this.pickupLocation = pickupLocation;
    this.dropOffLocation = dropOffLocation;
    this.pickupTime = pickupTime;
    this.dropOffTime = dropOffTime;
    this.distance = distance;
    this.driverId = driverId;
    this.route = new LinkedHashSet<RouteBean>();
    this.gpsDataPoints = new ArrayList<JSONObject>();
    averageSpeedOfTrip = distance * 1.0e6 / (dropOffTime.getMillis() - pickupTime.getMillis());
  }

  @Override
  public boolean equals(Object o) {
    if (o instanceof BookingBean) return false;
    else return this.trajectoryCOde.equals(((BookingBean) o).trajectoryCOde);
  }

  @Override
  public int hashCode() {
    return this.trajectoryCOde.hashCode();
  }

  public LinkedHashSet<RouteBean> getRoute() {
    return route;
  }

  public void setRoute(LinkedHashSet<RouteBean> route) {
    this.route = route;
  }

  public List<JSONObject> getGPSDataPoints() {
    return gpsDataPoints;
  }

  public String gettrajectoryCOde() {
    return trajectoryCOde;
  }

  public Coordinate getPickupLocation() {
    return pickupLocation;
  }

  public Coordinate getDropOffLocation() {
    return dropOffLocation;
  }

  public double getDistance() {
    return distance;
  }

  public DateTime getPickupTime() {
    return pickupTime;
  }

  public DateTime getDropOffTime() {
    return dropOffTime;
  }

  public Long getDriverId() {
    return driverId;
  }

  public int getHourOfDayLocal() {
    return hourOfDayLocal;
  }

  public void setHourOfDay(int hourOfDayLocal) {
    this.hourOfDayLocal = hourOfDayLocal;
  }

  /**
   * returns the average speed of trip
   *
   * @return average speed of trip in milliseconds.
   */
  public double getAverageSpeedOfTrip() {
    return averageSpeedOfTrip;
  }
}
