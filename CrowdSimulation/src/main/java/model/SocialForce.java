package model;

import java.util.Collection;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.math.Vector2D;
import com.vividsolutions.jts.operation.distance.DistanceOp;

import entities.Agent;
import entities.Obstacle;
import entities.Pedestrian;
import main.CrowdSimCore;

public class SocialForce {

    /**
     * The minimum length of the preferred velocity vector before the agent is
     * considered to be intending to move (and therefore can be perturbed when
     * {@link #avoidDeadlock} is true).
     */
    public final static double SOCIAL_FORCE_EPSILON = 0.00001;

    /**
     * A parameter of Social Force.
     * <p>
     * Recommended value: 2000 (N)
     */
    private final double a;

    /**
     * B parameter of Social Force.
     * <p>
     * Recommended value: 0.08 (m)
     */
    private final double b;

    /**
     * Tau parameter of Social Force. Represents the time taken for an agent to
     * adapt its current velocity to its preferred velocity. (Some people take
     * some time to change their velocity. This value might be personalized, but
     * currently we set it the same for all agents in the simulation.)
     * <p>
     * <b>This is not equivalent to time horizon, even though the unit is also
     * time.</b> Time horizon is how far into the future a collision avoidance
     * algorithm looks ahead for collision avoidance. Social Force only
     * considers the forces felt at the present moment, thus it has no need of a
     * time horizon parameter, (or we can say, it uses a time horizon of 0).
     * <p>
     * Recommended value: 0.5 (seconds)
     */
    private final double tau;

    /**
     * K1 parameter of Social Force. Not really sure.
     * <p>
     * Recommended value: 1.2E5 (kg.s-2)
     */
    private final double k1;
    /**
     * K2 parameter of Social Force. Not really sure.
     * <p>
     * Recommended value: 2.4E5 (kg.m-1.s-1)
     */
    private final double k2;
    /**
     * C parameter of Social Force. Agent's mass / C = agent's radius. (Since we
     * already know the radius of our agent, we reverse this equation to get the
     * mass.)
     * <p>
     * Recommended value: 320 (kg.m-1)
     */
    private final double c;

    /**
     * Perturbs the preferred velocity to avoid deadlocks due to perfect
     * symmetry. This means that the preferred velocity is randomized a bit.
     * Which may be undesirable.
     * <p>
     * Recommended value: true.
     */
    private boolean avoidDeadlock;

    /**
     * Performs some additional computations to further prevent agents from
     * colliding through obstacles. This is required because there are some
     * cases whereby the algorithm is unable to compute collision-free
     * trajectories with obstacles. By performing additional computations, the
     * chance of collision with obstacles is largely reduced. The performance
     * cost is minimal.
     * <p>
     * Recommended value: true.
     */
    private final boolean forceNonCollisionWithObstacles;

    /**
     * This determines if the obstacle repulsion force is only applied for the
     * nearest obstacle line segment, while ignoring the others.
     * <p>
     * This is required if you have obstacles that are composed of numerous
     * small line segments, and you do not want the force to be additively added
     * for each segment. This is a problem, for instance, when an agent goes
     * through a gap (e.g., doorway) in a double-hulled wall, where each side of
     * the gap consists of more than 1 line segment (i.e., the exterior hull,
     * the interior hull, and the very small segment spanning the width of the
     * hull). With this flag, the agent only encounters one segment; whereas
     * without this flag it may encounter 6 segments (3 on each side), and thus
     * causing it to never be able to pass through the gap.
     * <p>
     * Recommended value: true.
     */
    private final boolean repulseNearestObstacleLineOnly;

    /**
     * Recommended value from the paper.
     */
    public static final double DEFAULT_A = 2000;
    /**
     * Recommended value from the paper.
     */
    public static final double DEFAULT_B = 0.08;
    /**
     * Recommended value from the paper.
     */
    public static final double DEFAULT_TAU = 0.5;
    /**
     * Recommended value from the paper.
     */
    public static final double DEFAULT_K1 = 1.2E5;
    /**
     * Recommended value from the paper.
     */
    public static final double DEFAULT_K2 = 2.4E5;
    /**
     * Recommended value from the paper.
     */
    public static final double DEFAULT_C = 320;

    public static final boolean DEFAULT_AVOID_DEADLOCK = true;
    public static final boolean DEFAULT_FORCE_NON_COLLISION_WITH_OBSTACLES = true;
    public static final boolean DEFAULT_REPULSE_NEAREST_OBSTACLE_LINE_ONLY = true;
    private static final GeometryFactory geometryFactory = new GeometryFactory();

    /**
     * Constructs the SocialForce instance, with the following properties.
     * 
     * @param a
     *            See {@link #getA()}
     * @param b
     *            See {@link #getB()}
     * @param tau
     *            See {@link #getTau()}
     * @param k1
     *            See {@link #getK1()}
     * @param k2
     *            See {@link #getK2()}
     * @param c
     *            See {@link #getC()}
     * @param avoidDeadlock
     *            See {@link #isAvoidDeadlock()}
     * @param forceNonCollisionWithObstacles
     *            See {@link #isForceNonCollisionWithObstacles()}
     * @param repulseNearestObstacleLineOnly
     *            See {@link #isRepulseNearestObstacleLineOnly()}
     */
    public SocialForce(double a, double b, double tau, double k1, double k2, double c, boolean avoidDeadlock,
	    boolean forceNonCollisionWithObstacles, boolean repulseNearestObstacleLineOnly) {
	this.a = a;
	this.b = b;
	this.tau = tau;
	this.k1 = k1;
	this.k2 = k2;
	this.c = c;
	this.avoidDeadlock = avoidDeadlock;
	this.forceNonCollisionWithObstacles = forceNonCollisionWithObstacles;
	this.repulseNearestObstacleLineOnly = repulseNearestObstacleLineOnly;
    }

    /**
     * Constructs the SocialForce instance, with these recommended properties.
     * <dl>
     * <dt>{@link #getA()}
     * <dd>{@value #DEFAULT_A}
     * <dt>{@link #getB()}
     * <dd>{@value #DEFAULT_B}
     * <dt>{@link #getTau()}
     * <dd>{@value #DEFAULT_TAU}
     * <dt>{@link #getK1()}
     * <dd>{@value #DEFAULT_K1}
     * <dt>{@link #getK2()}
     * <dd>{@value #DEFAULT_K2}
     * <dt>{@link #getC()}
     * <dd>{@value #DEFAULT_C}
     * <dt>{@link #isAvoidDeadlock()}
     * <dd>{@value #DEFAULT_AVOID_DEADLOCK}
     * <dt>{@link #isForceNonCollisionWithObstacles()}
     * <dd>{@value #DEFAULT_FORCE_NON_COLLISION_WITH_OBSTACLES}
     * <dt>{@link #isRepulseNearestObstacleLineOnly()}
     * <dd>{@value #DEFAULT_REPULSE_NEAREST_OBSTACLE_LINE_ONLY}
     */
    public SocialForce() {
	this(DEFAULT_A, DEFAULT_B, DEFAULT_TAU, DEFAULT_K1, DEFAULT_K2, DEFAULT_C, DEFAULT_AVOID_DEADLOCK,
		DEFAULT_FORCE_NON_COLLISION_WITH_OBSTACLES, DEFAULT_REPULSE_NEAREST_OBSTACLE_LINE_ONLY);
    }

    /**
     * Collision avoidance method for Social Force, required for the
     * {@link CollisionAvoidance} interface.
     * <p>
     * Delegates to
     * {@link #socialForceAlgorithm(MobileEntity2D, Collection, Collection, Vector2d, double, double, RandomEngine, double, double, double, double, double, double, Vector2d, boolean, boolean, boolean)}
     * .
     */
    public Vector2D computeVelocity(final Agent agent, Collection<? extends Agent> neighbours, Obstacle obstacle,
	    double maxSpeed, double collisionTimeStep) {
	return socialForceAlgorithm(agent, neighbours, obstacle, maxSpeed, collisionTimeStep);
    }

    public Vector2D socialForceAlgorithm(Agent agent, Collection<? extends Agent> neighbours, Obstacle obstacle,
	    double maxSpeed, double collisionTimeStep) {

	Vector2D preferredVelocity = ((Pedestrian) agent).getPreferredVelocity();
	double preferredSpeed = preferredVelocity.length();
	if (avoidDeadlock) {
	    if (preferredVelocity.lengthSquared() > SOCIAL_FORCE_EPSILON) {

		double perturbSpeed = CrowdSimCore.RANDOM.nextDouble()
			* (0.05 * (preferredSpeed * 0.7 + maxSpeed * 0.3));
		double perturbAngle = CrowdSimCore.RANDOM.nextDouble() * 2 * Math.PI;
		preferredVelocity.add(
			new Vector2D(Math.sin(perturbAngle) * perturbSpeed, Math.cos(perturbAngle) * perturbSpeed));
		double newSpeed = preferredVelocity.length();
		if (newSpeed > maxSpeed) {
		    double scaleFactor = maxSpeed / newSpeed;
		    Vector2D newPrefferedVelocity = new Vector2D(preferredVelocity.getX() * scaleFactor,
			    preferredVelocity.getY() * scaleFactor);
		    ((Pedestrian) agent).setPreferredVelocity(newPrefferedVelocity);
		}
	    }
	}

	// Total force of other agents.
	Vector2D totalForce = new Vector2D();
	for (Agent neighbour : neighbours) {
	    computeAgentForce(agent, neighbour, totalForce);
	}

	if (obstacle != null)
	    computeObstacleLineForce(agent, obstacle, totalForce);

	double dvtx = (preferredVelocity.getX() - agent.getVelocity().getX()) / tau;
	double dvty = (preferredVelocity.getY() - agent.getVelocity().getY()) / tau;
	dvtx += totalForce.getX() / ((Pedestrian) agent).getMass();
	dvty += totalForce.getY() / ((Pedestrian) agent).getMass();
	if (Double.isNaN(dvtx))
	    throw new ArithmeticException("dvtx");
	if (Double.isNaN(dvtx))
	    throw new ArithmeticException("dvty");

	double vx = agent.getVelocity().getX() + dvtx * collisionTimeStep;
	double vy = agent.getVelocity().getY() + dvty * collisionTimeStep;

	double dv = Math.sqrt(vx * vx + vy * vy);
	if (dv > maxSpeed) {
	    vx *= maxSpeed / dv;
	    vy *= maxSpeed / dv;
	    dv = maxSpeed;
	}

	if (obstacle != null && forceNonCollisionWithObstacles && dv > 0) {
	    double dispX = vx / dv * ((Pedestrian) agent).getRadius() + vx * collisionTimeStep;
	    double dispY = vy / dv * ((Pedestrian) agent).getRadius() + vy * collisionTimeStep;
	    // Jinghui's force non-collision. Different from my RVO2's version
	    // (which is more thorough).
	    double mint = 1;
	    Vector2D displacement = new Vector2D(dispX, dispY);
	    Vector2D lineVector = new Vector2D(obstacle.getObstacleAsLine().p1);
	    lineVector = lineVector.subtract(new Vector2D(obstacle.getObstacleAsLine().p0));

	    Coordinate[] coords = { obstacle.getObstacleAsLine().p0,
		    new Coordinate(displacement.getX(), displacement.getY()),
		    new Coordinate(lineVector.getX(), lineVector.getY()) };

	    Point intersection = (Point) geometryFactory.createLineString(coords)
		    .intersection(geometryFactory.createPoint(agent.getPosition()));

	    if (intersection != null) {
		double intersectionDist = intersection.distance(geometryFactory.createPoint(agent.getPosition()))
			- ((Pedestrian) agent).getRadius();
		double tt = intersectionDist / dv / collisionTimeStep;
		if (tt < mint)
		    mint = tt;
	    }

	    if (mint < 1) {
		if (mint < -0.1)
		    mint = -0.1;
		vx *= mint * 0.5;
		vy *= mint * 0.5;
	    }

	    dv = Math.sqrt(vx * vx + vy * vy);
	    if (dv > maxSpeed) {
		vx *= maxSpeed / dv;
		vy *= maxSpeed / dv;
		dv = maxSpeed;
	    }
	}

	return new Vector2D(vx, vy);

    }

    /**
     * Computes the agent force between agent & neighbor, and adds that into
     * totalForce.
     * <p>
     * This is equation 3 from "Simulating dynamic features of escape panic".
     * <p>
     * Note that the agents radii are not computed from their mass as from
     * original SF, but rather we obtain it from the instances directly.
     * <p>
     * 
     * @param agent
     * @param neighbour
     * @param a
     * @param b
     * @param k1
     * @param k2
     * @param totalForce
     */
    public void computeAgentForce(Agent agent, Agent neighbour, Vector2D totalForce) {
	double d = agent.getPosition().distance(neighbour.getPosition()) + 1E-15;
	double deltaD = ((Pedestrian) agent).getRadius() + ((Pedestrian) neighbour).getRadius() - d;
	double fexp = a * Math.exp(deltaD / b);
	double fkg = deltaD < 0 ? 0 : k1 * deltaD;
	double nijx = (agent.getPosition().x - neighbour.getPosition().x) / d;
	double nijy = (agent.getPosition().y - neighbour.getPosition().y) / d;
	double fnijx = (fexp + fkg) * nijx;
	double fnijy = (fexp + fkg) * nijy;
	double fkgx = 0;
	double fkgy = 0;
	if (deltaD > 0) {
	    double tix = -nijy;
	    double tiy = nijx;
	    fkgx = fkgy = k2 * deltaD;
	    double deltaVij = (neighbour.getVelocity().getX() - agent.getVelocity().getX()) * tix
		    + (neighbour.getVelocity().getY() - agent.getVelocity().getY()) * tiy;
	    fkgx = fkgx * deltaVij * tix;
	    fkgy = fkgy * deltaVij * tiy;
	}
	totalForce.add(new Vector2D(fnijx + fkgx, fnijy + fkgy));
    }

    /**
     * Computes the force between an agent and obstacle line.
     * 
     * @param agent
     * @param obstacle
     * @param a
     * @param b
     * @param k1
     * @param k2
     * @param totalForce
     */
    public void computeObstacleLineForce(Agent agent, Obstacle obstacle, Vector2D totalForce) {
	// double diw =
	// GeometryUtils.calculateDistanceToLineSegment(obstacleLine.getStart(),
	// obstacleLine.getEnd(),
	// agent.getPosition(), null, pointPerpendicular);
	Coordinate line[] = { obstacle.getObstacleAsLine().getCoordinate(0),
		obstacle.getObstacleAsLine().getCoordinate(1) };

	Coordinate pointPerpendicular = DistanceOp.nearestPoints(geometryFactory.createPoint(agent.getPosition()),
		geometryFactory.createLineString(line))[1];

	double diw = obstacle.getObstacleAsLine().distance(agent.getPosition());

	double virDiw = obstacle.getObstacleAsLine().distance(agent.getPosition());

	if (virDiw == 0)
	    virDiw = 1E-15;
	double niwx = (agent.getPosition().x - pointPerpendicular.x) / virDiw;
	double niwy = (agent.getPosition().y - pointPerpendicular.y) / virDiw;
	double drw = ((Pedestrian) agent).getRadius() - diw; // Note: we use
							     // radius directly
	// instead of mass/c_mass.
	double fiw1 = a * Math.exp(drw / b);

	if (drw > 0) {
	    fiw1 += k1 * drw;
	}

	double fniwx = fiw1 * niwx;
	double fniwy = fiw1 * niwy;

	double fiwKgx = 0;
	double fiwKgy = 0;

	if (drw > 0) {
	    double fiwKg = k2 * drw * (agent.getVelocity().getX() * (-niwy) + agent.getVelocity().getY() * niwx);
	    fiwKgx = fiwKg * -niwy;
	    fiwKgy = fiwKg * niwx;
	}

	totalForce.add(new Vector2D(fniwx - fiwKgx, fniwy - fiwKgy));
    }

}
