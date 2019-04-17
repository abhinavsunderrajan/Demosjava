package testlma;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.apache.commons.math3.fitting.leastsquares.LeastSquaresBuilder;
import org.apache.commons.math3.fitting.leastsquares.LeastSquaresOptimizer;
import org.apache.commons.math3.fitting.leastsquares.LevenbergMarquardtOptimizer;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.KShortestPaths;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Test network for optimization trials.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class OptimizationTrial {

    private SimpleDirectedWeightedGraph<String, TestEdge> directedGraph;
    private Random random = new Random(42);
    private static final double NOISE_SD = 5.0;

    public SimpleDirectedWeightedGraph<String, TestEdge> getTestNetwork() {
	directedGraph = new SimpleDirectedWeightedGraph<String, TestEdge>(TestEdge.class);
	directedGraph.addVertex("a");
	directedGraph.addVertex("b");
	directedGraph.addVertex("c");
	directedGraph.addVertex("d");
	directedGraph.addVertex("e");
	directedGraph.addVertex("f");

	// for a
	TestEdge ab = directedGraph.addEdge("a", "b");
	ab.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ab, 3.0);
	TestEdge ba = directedGraph.addEdge("b", "a");
	ba.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ba, 3.0);

	TestEdge ac = directedGraph.addEdge("a", "c");
	ac.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ac, 5.0);
	TestEdge ca = directedGraph.addEdge("c", "a");
	ca.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ca, 5.0);

	TestEdge ad = directedGraph.addEdge("a", "d");
	ad.setExpectedSpeed(85.0);
	directedGraph.setEdgeWeight(ad, 9.0);
	TestEdge da = directedGraph.addEdge("d", "a");
	da.setExpectedSpeed(85.0);
	directedGraph.setEdgeWeight(da, 9.0);

	// for b
	TestEdge bc = directedGraph.addEdge("b", "c");
	bc.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(bc, 3.0);
	TestEdge cb = directedGraph.addEdge("c", "b");
	cb.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(cb, 3.0);

	TestEdge bd = directedGraph.addEdge("b", "d");
	bd.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(bd, 4.0);
	TestEdge db = directedGraph.addEdge("d", "b");
	db.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(db, 4.0);

	TestEdge be = directedGraph.addEdge("b", "e");
	be.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(be, 7.0);

	TestEdge eb = directedGraph.addEdge("e", "b");
	eb.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(eb, 7.0);

	// for c
	TestEdge cd = directedGraph.addEdge("c", "d");
	cd.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(cd, 2.0);

	TestEdge dc = directedGraph.addEdge("d", "c");
	dc.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(dc, 2.0);

	TestEdge ce = directedGraph.addEdge("c", "e");
	ce.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ce, 6.0);
	TestEdge ec = directedGraph.addEdge("e", "c");
	ec.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ec, 6.0);

	TestEdge cf = directedGraph.addEdge("c", "f");
	cf.setExpectedSpeed(85.0);
	directedGraph.setEdgeWeight(cf, 8.0);
	TestEdge fc = directedGraph.addEdge("f", "c");
	fc.setExpectedSpeed(85.0);
	directedGraph.setEdgeWeight(fc, 8.0);

	// for d
	TestEdge de = directedGraph.addEdge("d", "e");
	de.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(de, 2.0);
	TestEdge ed = directedGraph.addEdge("e", "d");
	ed.setExpectedSpeed(45.0);
	directedGraph.setEdgeWeight(ed, 2.0);
	TestEdge df = directedGraph.addEdge("d", "f");
	df.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(df, 2.0);

	TestEdge fd = directedGraph.addEdge("f", "d");
	fd.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(fd, 2.0);

	// for e
	TestEdge ef = directedGraph.addEdge("e", "f");
	ef.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(ef, 5.0);

	TestEdge fe = directedGraph.addEdge("f", "e");
	fe.setExpectedSpeed(55.0);
	directedGraph.setEdgeWeight(fe, 5.0);

	return directedGraph;

    }

    public List<TestRoute> generateRoutes(int numberOfRoutes) {

	int vertexSize = directedGraph.vertexSet().size();
	KShortestPaths<String, TestEdge> ksp = new KShortestPaths<String, TestEdge>(directedGraph, 4);
	List<TestRoute> testRouteList = new ArrayList<TestRoute>();

	for (int i = 0; i < numberOfRoutes; i++) {
	    String from = null;
	    String to = null;
	    int fromIndex = random.nextInt(vertexSize);
	    int toIndex = fromIndex;
	    do {
		toIndex = random.nextInt(vertexSize);
	    } while (toIndex == fromIndex);

	    int index = 0;
	    for (String vertex : directedGraph.vertexSet()) {
		if (index == toIndex)
		    from = vertex;
		if (index == fromIndex)
		    to = vertex;
		if (from != null && to != null)
		    break;
		index++;
	    }

	    GraphPath<String, TestEdge> path = ksp.getPaths(from, to).get(random.nextInt(4));
	    if (path == null)
		continue;

	    List<Double> travelTimeList = new ArrayList<>(path.getEdgeList().size());
	    for (TestEdge edge : path.getEdgeList()) {
		double speed = edge.getExpectedSpeed() + random.nextGaussian() * NOISE_SD;
		travelTimeList.add(directedGraph.getEdgeWeight(edge) / speed);
	    }
	    testRouteList.add(new TestRoute(path.getEdgeList(), travelTimeList));

	}

	return testRouteList;

    }

    public static void main(String args[]) throws IOException {
	OptimizationTrial ts = new OptimizationTrial();
	SimpleDirectedWeightedGraph<String, TestEdge> graph = ts.getTestNetwork();
	System.out.println("Number of edges in graph " + graph.edgeSet().size());
	List<TestRoute> routesAndTravelTimes = ts.generateRoutes(300);

	BufferedWriter bw = new BufferedWriter(new FileWriter("routes-test.csv"));
	for (TestRoute route : routesAndTravelTimes) {
	    StringBuffer buffer = new StringBuffer();
	    for (int i = 0; i < route.getRoute().size(); i++) {
		if (i == route.getRoute().size() - 1)
		    buffer.append(route.getRoute().get(i));
		else
		    buffer.append(route.getRoute().get(i) + "\t");
	    }
	    bw.write(buffer + "," + route.getTotalTravelTime() + "\n");
	}
	bw.flush();
	bw.close();

	// prepare construction of LeastSquresProblem by builder
	LeastSquaresBuilder lsb = new LeastSquaresBuilder();
	// set model function and its jacobian

	TravelTimeRouteFunction qf = new TravelTimeRouteFunction(routesAndTravelTimes, graph);
	lsb.model(qf.retMVF(), qf.retMMF());
	double[] newTarget = qf.calculateTarget();

	// set target data
	lsb.target(newTarget);
	double[] newStart = new double[graph.edgeSet().size()];
	for (int i = 0; i < newStart.length; i++)
	    newStart[i] = 0.1;

	lsb.start(newStart);
	lsb.maxEvaluations(100);
	lsb.maxIterations(1000);

	// construct LevenbergMarquardtOptimizer
	LevenbergMarquardtOptimizer lmo = new LevenbergMarquardtOptimizer();
	try {
	    // do LevenbergMarquardt optimization
	    LeastSquaresOptimizer.Optimum lsoo = lmo.optimize(lsb.build());
	    final double[] optimalValues = lsoo.getPoint().toArray();

	    final double optimalValuesPython[] = { 0.06128682, 0.06072546, 0.10119065, 0.09612515, 0.11813575,
		    0.10179283, 0.06722445, 0.07174919, 0.07209159, 0.07764845, 0.16060969, 0.1682257, 0.04799132,
		    0.04519669, 0.12609025, 0.11919371, 0.10217543, 0.08627742, 0.04847219, 0.0429571, 0.03574447,
		    0.03779286, 0.09842833, 0.09295077 };
	    int i = 0;
	    double mape = 0.0;
	    for (TestEdge edge : graph.edgeSet()) {
		double actualSpeed = edge.getExpectedSpeed();
		double predictedSpeed = graph.getEdgeWeight(edge) / optimalValues[i];
		System.out.println(edge + " expected speed:" + actualSpeed + " speed from lma: " + predictedSpeed);
		mape += Math.abs(actualSpeed - predictedSpeed) / actualSpeed;
		i++;
	    }

	    mape = mape * 100.0 / graph.edgeSet().size();
	    System.out.println("MAPE:" + mape + "% noise:" + NOISE_SD + " km/hr");
	    System.out.println("Iteration number: " + lsoo.getIterations());
	    System.out.println("Evaluation number: " + lsoo.getEvaluations());
	} catch (Exception e) {
	    e.printStackTrace();
	}

    }

}
