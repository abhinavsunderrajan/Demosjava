package lmaroadprofile;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Road speed profiling using the adaptive time bins (based on minute of day)
 * 
 * @author abhinav.sunderrajan
 *
 */
public class GenerateJSONFileForOptimization {
    private static final List<Integer> BINS = Arrays.asList(0, 43, 91, 359, 388, 435, 450, 479, 527, 586, 617, 685, 984,
	    1018, 1044, 1103, 1205, 1342, 1389, 1440);
    private static JSONObject bookingcodeRoutes = new JSONObject();

    public static void main(String[] args) throws IOException {

	for (Integer bin : BINS) {
	    JSONArray routeObjectArr = new JSONArray();
	    bookingcodeRoutes.put(bin.toString(), routeObjectArr);
	}

	try (Stream<Path> paths = Files.walk(
		Paths.get("/Users/abhinav.sunderrajan/Desktop/road-speed-profiling/abhinav/deployment/str_results/"))) {
	    paths.filter(Files::isRegularFile).forEach(fileName -> {
		if (fileName.toString().contains(".csv")) {
		    try {
			BufferedReader br = new BufferedReader(new FileReader(new File(fileName.toString())));
			while (br.ready()) {
			    String line = br.readLine();
			    if (line.contains("booking_code"))
				continue;

			    String[] split = line.split("\t");
			    double ape = Double.parseDouble(split[1]);
			    if (ape > 0.3)
				continue;

			    JSONObject bookingObj = new JSONObject();
			    bookingObj.put("booking_code", split[0]);
			    String strResults[] = split[4].split(",");
			    JSONArray routeObjArray = null;
			    long tripStartTime = -1;
			    long tripEndTime = -1;

			    for (int count = 0; count < strResults.length; count++) {
				String dataPoint[] = strResults[count].split("_");
				// Consider only the first point for the minute
				// of day
				if (count == 0) {
				    tripStartTime = Long.parseLong(dataPoint[1]);
				    DateTime dt = new DateTime(tripStartTime * 1000);
				    bookingObj.put("trip_distance_meters", Double.parseDouble(split[2]) * 1000.0);
				    int minuteOfDay = dt.getMinuteOfDay();
				    for (int bin = 1; bin < BINS.size(); bin++) {
					if (minuteOfDay < BINS.get(bin)) {
					    routeObjArray = bookingcodeRoutes
						    .getJSONArray(BINS.get(bin - 1).toString());
					    JSONArray route = new JSONArray();
					    bookingObj.put("route", route);
					    break;
					}
				    }
				}

				if (count == strResults.length - 1)
				    tripEndTime = Long.parseLong(dataPoint[1]);

				Integer roadId = Integer.parseInt(dataPoint[0]);
				bookingObj.getJSONArray("route").put(roadId);

			    }
			    Long travelTime = tripEndTime - tripStartTime;

			    if (travelTime > 0) {
				double avgSpeed = bookingObj.getDouble("trip_distance_meters") / travelTime;
				if (avgSpeed < 30.0) {
				    bookingObj.put("travel_time", travelTime.intValue());
				    routeObjArray.put(bookingObj);
				}
			    }
			}
			br.close();
			System.out.println("Finished reading file " + fileName);

		    } catch (Exception e) {
			e.printStackTrace();
		    }

		}
	    });

	}

	FileWriter file = new FileWriter("booking-codes-optimization_91.txt");
	BufferedWriter bw = new BufferedWriter(file);
	bw.write(bookingcodeRoutes.getJSONArray("91").toString());
	bw.flush();
	bw.close();

    }

}
