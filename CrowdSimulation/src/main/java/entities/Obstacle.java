package entities;

import com.vividsolutions.jts.geom.LineSegment;

public class Obstacle {

    private LineSegment obstacleAsLine;

    public LineSegment getObstacleAsLine() {
	return obstacleAsLine;
    }

    public void setObstacleAsLine(LineSegment obstacleAsLine) {
	this.obstacleAsLine = obstacleAsLine;
    }

}
