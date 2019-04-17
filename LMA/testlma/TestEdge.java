package testlma;

import org.jgrapht.graph.DefaultWeightedEdge;

/**
 * 
 * @author abhinav.sunderrajan
 *
 */
public class TestEdge extends DefaultWeightedEdge {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private double expectedSpeed;

    public double getExpectedSpeed() {
	return expectedSpeed;
    }

    public void setExpectedSpeed(double expectedSpeed) {
	this.expectedSpeed = expectedSpeed;
    }

}
