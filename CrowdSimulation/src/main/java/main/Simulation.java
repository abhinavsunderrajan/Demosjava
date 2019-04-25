package main;

import java.util.Iterator;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

import entities.Agent;
import entities.Pedestrian;
import model.SocialForce;
import viz.CrowdSimViz;

public class Simulation {

    private long simulationTime;
    private static final long FINISH_TIME = 600000;
    private static final int TIME_STEP = 20;
    private SocialForce socialForce;
    private CrowdSimCore core;
    private CrowdSimViz viz;

    public Simulation(CrowdSimCore core) {
	this.core = core;
	socialForce = new SocialForce();
	simulationTime = 0;
    }

    public void runSimulation() throws InterruptedException {
	for (simulationTime = 0; simulationTime <= FINISH_TIME; simulationTime += TIME_STEP) {

	    if (core.getAgents().isEmpty()) {
		System.out.println(
			"All agents have reached destination at time " + (simulationTime / 1000.0) + " seconds");
		return;
	    }

	    core.getAgents().parallelStream().forEach(agent -> {
		List<Agent> neighbors = ((Pedestrian) agent).getNeighbors();
		Vector2D velocity = socialForce.computeVelocity(agent, neighbors, null, 1.5, 0.5);
		agent.setVelocity(velocity);
	    });

	    // update position of agents
	    Iterator<Agent> itr = core.getAgents().iterator();
	    while (itr.hasNext()) {
		Agent agent = itr.next();
		Coordinate deltaPos = new Coordinate(agent.getVelocity().multiply((double) TIME_STEP / 1000.0).getX(),
			agent.getVelocity().multiply((double) TIME_STEP / 1000.0).getY());
		Coordinate curPos = agent.getPosition();
		agent.setPosition(new Coordinate(curPos.x + deltaPos.x, curPos.y + deltaPos.y));
		Pedestrian pedestrian = (Pedestrian) agent;

		// if agent has reached it's destination then remove it from
		// the simulation.
		Coordinate destinationCoord = pedestrian.getPath().getCoordinateN(1);

		if (agent.getPosition().equals2D(destinationCoord)
			|| agent.getPosition().distance(destinationCoord) < 0.5) {
		    synchronized (viz.getLock()) {
			itr.remove();
		    }
		    System.out.println("Removed agent: " + agent.getAgentId() + " at time " + simulationTime);
		}
	    }

	    if (viz != null) {
		viz.getFrame().repaint();
		Thread.sleep(2);
	    }

	    // update preferred velocities of agents.

	    core.getAgents().parallelStream().forEach(agent -> {
		Pedestrian pedestrian = (Pedestrian) agent;
		Coordinate destination = pedestrian.getPath().getCoordinateN(1);

		double xVec = (destination.x - agent.getPosition().x) / destination.distance(agent.getPosition());
		double yVec = (destination.y - agent.getPosition().y) / destination.distance(agent.getPosition());
		Vector2D preferredVelocity = new Vector2D(xVec, yVec).multiply(Pedestrian.DESIRED_SPEED);
		pedestrian.setPreferredVelocity(preferredVelocity);
	    });

	    // update the neighbors
	    core.updateAgentNeighbors();

	}
    }

    public void setVisualization(CrowdSimViz environment) {
	this.viz = environment;

    }

}
