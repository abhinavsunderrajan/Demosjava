package entities;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.math.Vector2D;

public abstract class Agent {

    protected Vector2D velocity;
    protected Coordinate position;
    protected int agentId;

    public Agent(int agentId) {
	this.agentId = agentId;
    }

    public Vector2D getVelocity() {
	return velocity;
    }

    public void setVelocity(Vector2D velocity) {
	this.velocity = velocity;
    }

    public Coordinate getPosition() {
	return position;
    }

    public void setPosition(Coordinate position) {
	this.position = position;
    }

    public int getAgentId() {
	return agentId;
    }

    @Override
    public boolean equals(Object o) {
	if (o instanceof Agent) {
	    return this.agentId == ((Agent) o).agentId;
	} else {
	    return false;
	}

    }

    @Override
    public int hashCode() {
	return this.agentId;
    }

}
