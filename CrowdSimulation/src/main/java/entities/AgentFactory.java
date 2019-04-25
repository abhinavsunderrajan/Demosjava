package entities;

import main.CrowdSimCore;

public class AgentFactory {

    private static final double MEAN_RADIUS = 0.5;
    private static final double VARIANCE_RADIUS = 0.05;
    private static int agentId = 1;

    public Agent getAgent(String agentType) {

	if (agentType.equalsIgnoreCase("PEDESTRIAN")) {

	    double radius = MEAN_RADIUS + VARIANCE_RADIUS * CrowdSimCore.RANDOM.nextGaussian();
	    return new Pedestrian(radius, agentId++);
	}
	return null;

    }

}
