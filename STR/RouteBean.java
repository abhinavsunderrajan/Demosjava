package route;

import networkmodel.Road;

public class RouteBean {

    private Road road;
    private long timeStamp;
    private double speed;

    @Override
    public boolean equals(Object o) {
	if (o instanceof RouteBean) {
	    return ((RouteBean) o).getRoad().getRoadId() == this.road.getRoadId();
	}
	return false;
    }

    @Override
    public int hashCode() {
	return road.getRoadId().hashCode();
    }

    public RouteBean(Road road, long timeStamp, double speed) {
	this.road = road;
	this.timeStamp = timeStamp;
	this.speed = speed;
    }

    public Road getRoad() {
	return road;
    }

    public long getTimeStamp() {
	return timeStamp;
    }

    public double getSpeed() {
	return speed;
    }

}
