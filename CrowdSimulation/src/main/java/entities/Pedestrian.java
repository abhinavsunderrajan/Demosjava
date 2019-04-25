package entities;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.math.Vector2D;

import model.SocialForce;

public class Pedestrian extends Agent {

    private double radius;
    private double mass;
    private List<Agent> neighbours;
    private Vector2D preferredVelocity;
    private LineString path;
    // desired speed of a pedestrian
    public static final double DESIRED_SPEED = 1.5;

    public Pedestrian(double radius, int pedestrianId) {
	super(pedestrianId);
	this.radius = radius;
	this.mass = radius * SocialForce.DEFAULT_C;
	neighbours = new ArrayList<Agent>();
    }

    public double getMass() {
	return mass;
    }

    public double getRadius() {
	return radius;
    }

    public List<Agent> getNeighbors() {
	return neighbours;
    }

    public Vector2D getPreferredVelocity() {
	return preferredVelocity;
    }

    public void setPreferredVelocity(Vector2D preferredVelocity) {
	this.preferredVelocity = preferredVelocity;
    }

    /**
     * The path agent intends to take at the beginning of the simulation.
     * 
     * @return
     */
    public LineString getPath() {
	return path;
    }

    /**
     * Set the path which the agent intends to take at the beginning of the
     * simulation.
     * 
     * @param path
     */
    public void setPath(LineString path) {
	this.path = path;
    }

}
