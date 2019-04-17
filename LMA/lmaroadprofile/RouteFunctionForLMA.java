package lmaroadprofile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.math3.analysis.MultivariateMatrixFunction;
import org.apache.commons.math3.analysis.MultivariateVectorFunction;
import org.json.JSONArray;

import networkmodel.Road;

/**
 * 
 * @author abhinav.sunderrajan
 *
 */
public class RouteFunctionForLMA {

    private JSONArray dataPoints;
    private Map<Long, Road> roadsWithSpeeds;

    public RouteFunctionForLMA(JSONArray dataPoints, Map<Long, Road> roadsWithSpeeds) {
	this.dataPoints = dataPoints;
	this.roadsWithSpeeds = roadsWithSpeeds;
    }

    /**
     * return target data as double array by target data
     * 
     * @return target double array of target data
     */
    public double[] calculateTarget() {
	double[] target = new double[dataPoints.length()];
	for (int i = 0; i < dataPoints.length(); i++)
	    target[i] = dataPoints.getJSONObject(i).getInt("travel_time");

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
		double[] values = new double[dataPoints.length()];
		for (int i = 0; i < dataPoints.length(); ++i) {
		    values[i] = 0.0;
		    List<Integer> route = new ArrayList<>();
		    JSONArray routeArr = dataPoints.getJSONObject(i).getJSONArray("route");
		    for (int count = 0; count < routeArr.length(); count++)
			route.add(routeArr.getInt(count));

		    int j = 0;
		    for (Long roadId : roadsWithSpeeds.keySet()) {
			if (route.contains(roadId))
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
		double[][] jacobian = new double[dataPoints.length()][roadsWithSpeeds.size()];
		for (int i = 0; i < dataPoints.length(); ++i) {
		    List<Integer> route = new ArrayList<>();
		    JSONArray routeArr = dataPoints.getJSONObject(i).getJSONArray("route");
		    for (int count = 0; count < routeArr.length(); count++)
			route.add(routeArr.getInt(count));

		    int j = 0;
		    for (Long roadId : roadsWithSpeeds.keySet()) {
			if (route.contains(roadId))
			    jacobian[i][j] = 1.0;
			j++;
		    }
		}

		return jacobian;
	    }

	};
    }

}
