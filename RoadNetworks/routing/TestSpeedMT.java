package routing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Random;

import org.json.JSONArray;
import org.json.JSONObject;

class Count extends Thread {
    private int id;

    Count(int i) {
	super("my extending thread");
	System.out.println("my thread created" + this + i);
	id = i;
	start();
    }

    public void run() {
	try {
	    long start_time = System.currentTimeMillis();
	    for (int i = 0; i < 100; i++) {
		System.out.println("Printing the count for thread " + id + " " + i);
		Random rand = new Random();
		int a = rand.nextInt(50) + 1;
		int b = rand.nextInt(50) + 1;
		String origin = a + "/";
		String dest = b + "/";
		String grab_url = "http://api.dev.grabds.info/routing-simulation/getRouting/";
		String url = grab_url + origin + dest + "0";
		BufferedReader rd = new BufferedReader(
			new InputStreamReader(new URL(url).openStream(), Charset.forName("UTF-8")));
		JSONObject response = new JSONObject(readAll(rd));
		JSONArray route = response.getJSONArray("route");
		Thread.sleep(1);
	    }
	    long end_time = System.currentTimeMillis();
	    String timeUsage = Long.toString(end_time - start_time);
	    System.out.println("The time usage is " + timeUsage);
	} catch (InterruptedException e) {
	    System.out.println("my thread interrupted");
	} catch (Exception e) {
	    System.out.println("Errors");
	}
	System.out.println("My thread run is over");
    }

    private static String readAll(Reader rd) throws IOException {
	StringBuilder sb = new StringBuilder();
	int cp;
	while ((cp = rd.read()) != -1) {
	    sb.append((char) cp);
	}
	return sb.toString();
    }
}

public class TestSpeedMT {
    private static String readAll(Reader rd) throws IOException {
	StringBuilder sb = new StringBuilder();
	int cp;
	while ((cp = rd.read()) != -1) {
	    sb.append((char) cp);
	}
	return sb.toString();
    }

    public static void initialize(int threadNum) {
	try {
	    // long timestamp = 10000;
	    // String grab_url = "http://localhost:9000/loadDataWithThread/";
	    // String url = grab_url + timestamp + "/" + threadNum;
	    // BufferedReader rd = new BufferedReader(
	    // new InputStreamReader(new URL(url).openStream(),
	    // Charset.forName("UTF-8")));
	} catch (Exception e) {
	}
    }

    public static void main(String args[]) {
	initialize(8);

	for (int i = 0; i < 128; i++) {
	    Count cnt = new Count(i);
	}

	try {
	    while (true) {
		System.out.println("Main thread will be alive till the child thread is live");
		Thread.sleep(100000);
	    }
	} catch (InterruptedException e) {
	    System.out.println("Main thread interrupted");
	}
	System.out.println("Main thread's run is over");
    }
}