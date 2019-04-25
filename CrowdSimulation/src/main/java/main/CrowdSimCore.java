package main;

import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;

import entities.Agent;
import entities.AgentFactory;
import entities.Pedestrian;
import viz.CrowdSimViz;

public class CrowdSimCore {

    private static CrowdSimCore crowdSim;
    private AgentFactory factory;
    private List<Agent> agents;
    private CrowdSimViz viz;

    private static final int NUM_OF_AGENTS = 400;
    private static final GeometryFactory gf = new GeometryFactory();
    public static final boolean HAVE_VIZ = true;
    public static final int ARENA_WIDTH = 400;
    public static final int ARENA_HEIGHT = 200;
    private static final double EXIT_WIDTH = 2.0;
    public static Random RANDOM;

    public static CrowdSimCore getCrowdSimInstance(long seed) {
	if (crowdSim == null) {
	    RANDOM = new Random(seed);
	    crowdSim = new CrowdSimCore(seed);
	}
	return crowdSim;
    }

    private CrowdSimCore(long seed) {
	factory = new AgentFactory();
	agents = new ArrayList<Agent>();
    }

    public static void main(String args[]) throws InterruptedException, ExecutionException {
	CrowdSimCore core = CrowdSimCore.getCrowdSimInstance(501l);

	// Create agents with random positions inside the simulation arena.
	for (int i = 0; i < NUM_OF_AGENTS; i++) {
	    Pedestrian agent = (Pedestrian) core.factory.getAgent("PEDESTRIAN");
	    // Initialize a random position inside the arena
	    double x = 0.0 + RANDOM.nextDouble() * ARENA_WIDTH;
	    double y = 0.0 + RANDOM.nextDouble() * ARENA_HEIGHT;
	    Coordinate initPosition = new Coordinate(x, y);
	    agent.setPosition(initPosition);

	    // Right begin at one spot and end at another spot
	    Coordinate[] coords = new Coordinate[2];
	    coords[0] = initPosition;
	    // All agents want to go to this point irrespective of where they
	    // start.
	    double destY = (ARENA_HEIGHT / 2.0 - EXIT_WIDTH / 2.0) + EXIT_WIDTH * RANDOM.nextDouble();
	    coords[1] = new Coordinate(ARENA_WIDTH, destY);

	    LineString path = gf.createLineString(coords);
	    double xVec = (coords[1].x - coords[0].x) / coords[1].distance(coords[0]);
	    double yVec = (coords[1].y - coords[0].y) / coords[1].distance(coords[0]);
	    Vector2D preferredVelocity = new Vector2D(xVec, yVec).multiply(Pedestrian.DESIRED_SPEED);
	    // Initialize the agents's velocity and preferred velocity to be the
	    // same.
	    agent.setVelocity(preferredVelocity);
	    agent.setPreferredVelocity(preferredVelocity);
	    agent.setPath(path);
	    core.agents.add(agent);

	}

	core.updateAgentNeighbors();
	Simulation simulation = new Simulation(core);

	if (HAVE_VIZ) {
	    core.viz = new CrowdSimViz(core);
	    simulation.setVisualization(core.viz);
	}

	simulation.runSimulation();
	System.out.println("Finished simulation..");
	if (HAVE_VIZ) {
	    core.viz.getFrame().dispatchEvent(new WindowEvent(core.viz.getFrame(), WindowEvent.WINDOW_CLOSING));
	}
    }

    public List<Agent> getAgents() {
	return agents;
    }

    /**
     * Update neighbors for all agents in the simulation. This implementation is
     * bad as of now. Will have to change when simulations works
     */
    public void updateAgentNeighbors() {
	// update neighbors for this agent. This implementation is bad as
	// of now. Will have to change when simulations works.

	agents.parallelStream().forEach(agent -> {
	    Pedestrian pedestrian = (Pedestrian) agent;
	    pedestrian.getNeighbors().clear();
	    for (Agent neighbour : agents) {
		if (!neighbour.equals(agent)) {
		    if (agent.getPosition().distance(neighbour.getPosition()) < 5.0) {
			pedestrian.getNeighbors().add(neighbour);
		    }
		}
	    }
	});

    }

}
