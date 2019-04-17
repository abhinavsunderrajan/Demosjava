package lmaroadprofile;

import java.util.ArrayList;
import java.util.List;

/**
 * Example of a data point for LMA algorithm.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class DataPointLMA {
    private String bookingCode;
    // think of this as a list of road Ids.
    private List<Integer> routeList;
    private double travelTime;

    public DataPointLMA(String bookingCode, double travelTime) {
	this.bookingCode = bookingCode;
	this.routeList = new ArrayList<>();
	this.travelTime = travelTime;
    }

    public String getBookingCode() {
	return bookingCode;
    }

    public List<Integer> getRouteEdgeList() {
	return routeList;
    }

    public double getTravelTime() {
	return travelTime;
    }

}
