package lmaroadprofile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.json.JSONArray;
import org.json.JSONTokener;
import org.json.simple.parser.ParseException;

import networkmodel.OSMRoadNetworkModel;
import networkmodel.Road;

/**
 * Optimization using the snap to road data.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class LMAOptimization {
    private static OSMRoadNetworkModel roadNetwork;
    // private static final String GEOHASH = "w21z7";

    public static void main(String[] args) throws FileNotFoundException, IOException, ParseException {

	String roadsFilePath = null;
	String nodesFilePath = null;
	String speedsFilePath = null;
	int numOfBookings = 10;

	if (args.length < 4) {
	    roadsFilePath = "src/main/resources/roadsSG.txt";
	    nodesFilePath = "src/main/resources/nodesSG.txt";
	    speedsFilePath = "/Users/abhinav.sunderrajan/Desktop/road-speed-profiling/abhinav/median-speeds/local/speeds/medianSpeedsLocal_91.txt";
	    numOfBookings = 20000;
	} else {
	    roadsFilePath = args[0];
	    nodesFilePath = args[1];
	    speedsFilePath = args[2];
	    numOfBookings = Integer.parseInt(args[3]);
	}

	roadNetwork = new OSMRoadNetworkModel(roadsFilePath, nodesFilePath, 6, 5, false, true);

	JSONTokener tokener = new JSONTokener(new FileReader("booking-codes-optimization_91.txt"));
	JSONArray completedBookingsArr = new JSONArray(tokener);
	// Iterator<Object> it = completedBookingsArr.iterator();
	// int count = 0;
	// while (it.hasNext()) {
	// Object obj = it.next();
	// if (count > numOfBookings)
	// it.remove();
	// count++;
	// }

	// prepare construction of LeastSquresProblem by builder
	LeastSquaresBuilder lsb = new LeastSquaresBuilder();
	// set model function and its jacobian

	System.out.println("Number of bookings for bin 91 is " + completedBookingsArr.length());

	Map<Long, Road> roadsWithSpeeds = getRoadsWithSpeeds(speedsFilePath);
	System.out.println("Number of roads with speeds:" + roadsWithSpeeds.size());

	// export data for scipy
	BufferedWriter bw = new BufferedWriter(new FileWriter("links-osm.csv"));
	bw.write("road-id\n");
	for (long roadId : roadsWithSpeeds.keySet())
	    bw.write(roadId + "\n");
	bw.flush();
	bw.close();
	bw = new BufferedWriter(new FileWriter("routes-travel-times-osm.csv"));
	bw.write("route,travel_time\n");

	for (int i = 0; i < completedBookingsArr.length(); i++) {
	    JSONArray routeArr = completedBookingsArr.getJSONObject(i).getJSONArray("route");
	    StringBuffer buffer = new StringBuffer();
	    for (int j = 0; j < routeArr.length(); j++) {
		if (j == routeArr.length() - 1)
		    buffer.append(routeArr.getInt(j));
		else
		    buffer.append(routeArr.getInt(j) + "\t");
	    }
	    bw.write(buffer + "," + completedBookingsArr.getJSONObject(i).getInt("travel_time") + "\n");

	}

	bw.flush();
	bw.close();

	// end of export data for scipy

	RouteFunctionForLMA qf = new RouteFunctionForLMA(completedBookingsArr, roadsWithSpeeds);
	lsb.model(qf.retMVF(), qf.retMMF());
	double[] newTarget = qf.calculateTarget();

	// set target data
	lsb.target(newTarget);
	double[] newStart = new double[roadsWithSpeeds.size()];
	for (int i = 0; i < newStart.length; i++)
	    newStart[i] = 300;

	System.exit(0);

	lsb.start(newStart);
	lsb.maxEvaluations(100);
	lsb.maxIterations(1000);

	// construct LevenbergMarquardtOptimizer
	LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
	// do LevenbergMarquardt optimization
	LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
	final double[] optimalValues = lsoo.getPoint().toArray();
	int i = 0;
	for (Road edge : roadsWithSpeeds.values()) {
	    double actualSpeed = edge.getExpectedSpeed();
	    double predictedSpeed = edge.getLength() / optimalValues[i];
	    System.out.println(edge + " expected speed:" + actualSpeed + " speed from lma: " + predictedSpeed);
	    i++;
	}

    }

    private static Map<Long, Road> getRoadsWithSpeeds(String speedsFilePath) throws NumberFormatException, IOException {
	Map<Long, Road> roadsWithSpeeds = new LinkedHashMap<>();

	BufferedReader br = new BufferedReader(new FileReader(new File(speedsFilePath)));
	while (br.ready()) {
	    String line = br.readLine();
	    if (line.contains("median"))
		continue;
	    String[] split = line.split("\t");
	    int numOfPoints = Integer.parseInt(split[6]);
	    if (numOfPoints >= 15) {
		Road road = roadNetwork.getAllRoadsMap().get(Integer.parseInt(split[0]));
		double median = Double.parseDouble(split[3]);
		double mean = Double.parseDouble(split[4]);
		road.setExpectedSpeed(median);
		roadsWithSpeeds.put(road.getRoadId(), road);

	    }

	}
	br.close();
	return roadsWithSpeeds;
    }

}
