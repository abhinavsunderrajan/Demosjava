package testlma;

import java.util.List;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.jgrapht.graph.SimpleDirectedWeightedGraph;

/**
 * Implementation of the jacabian matrix and optimization function for the
 * 
 * @author abhinav.sunderrajan
 *
 */
public class TravelTimeRouteFunction {

    private List<TestRoute> routesAndTravelTimes;
    private SimpleDirectedWeightedGraph<String, TestEdge> graph;

    public TravelTimeRouteFunction(List<TestRoute> routesAndTravelTimes,
	    SimpleDirectedWeightedGraph<String, TestEdge> graph) {
	this.graph = graph;
	this.routesAndTravelTimes = routesAndTravelTimes;

    }

    /**
     * return target data as double array by target data
     * 
     * @return target double array of target data
     */
    public double[] calculateTarget() {
	double[] target = new double[routesAndTravelTimes.size()];
	int i = 0;
	for (TestRoute testRoute : routesAndTravelTimes) {
	    target[i] = testRoute.getTotalTravelTime();
	    i++;
	}

	// StringBuffer bf = new StringBuffer("");
	// for (double y : target) {
	// bf.append(y + ",");
	// }
	// System.out.println(bf);
	return target;
    }

    /**
     * Define model function and return values
     * 
     * @return return the values of model function by input data
     */
    public MultivariateVectorFunction retMVF() {
	return new MultivariateVectorFunction() {
	    @Override
	    public double[] value(double[] variables) throws IllegalArgumentException {
		double[] values = new double[routesAndTravelTimes.size()];
		for (int i = 0; i < routesAndTravelTimes.size(); ++i) {
		    values[i] = 0.0;
		    List<TestEdge> route = routesAndTravelTimes.get(i).getRoute();
		    int j = 0;
		    for (TestEdge edge : graph.edgeSet()) {
			if (route.contains(edge))
			    values[i] += variables[j];
			j++;
		    }
		}
		return values;
	    }
	};
    }

    /**
     * Return the jacobian of the model function
     * 
     * @return return the jacobian
     */
    public MultivariateMatrixFunction retMMF() {
	return new MultivariateMatrixFunction() {

	    @Override
	    public double[][] value(double[] point) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return jacobian(point);
	    }

	    /**
	     * calculate and return jacobian
	     * 
	     * @param variables
	     *            parameters of model function
	     * @return jacobian of the model function
	     */
	    private double[][] jacobian(double[] variables) {
		double[][] jacobian = new double[routesAndTravelTimes.size()][graph.edgeSet().size()];
		for (int i = 0; i < routesAndTravelTimes.size(); ++i) {
		    List<TestEdge> route = routesAndTravelTimes.get(i).getRoute();
		    // List<Double> travelTimes =
		    // routesAndTravelTimes.get(i).getTravelTimeList();
		    int j = 0;
		    for (TestEdge edge : graph.edgeSet()) {
			if (route.contains(edge))
			    jacobian[i][j] = 1.0;
			j++;
		    }
		}

		return jacobian;
	    }

	};
    }

}
