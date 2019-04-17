package testlma;

import java.util.List;

/**
 * 
 * @author abhinav.sunderrajan
 *
 */
public class TestRoute {

    private List<TestEdge> route;
    private List<Double> travelTimeList;
    private double totalTravelTime;

    public TestRoute(List<TestEdge> route, List<Double> travelTimeList) {
	this.route = route;
	this.travelTimeList = travelTimeList;
	for (double edgeTime : travelTimeList)
	    totalTravelTime += edgeTime;

    }

    public List<TestEdge> getRoute() {
	return route;
    }

    public List<Double> getTravelTimeList() {
	return travelTimeList;
    }

    public double getTotalTravelTime() {
	return totalTravelTime;
    }

}
