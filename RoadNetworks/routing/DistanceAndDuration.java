package routing;

public class DistanceAndDuration {
    private double duration;
    private double distance;
    
    public DistanceAndDuration(double distance, double duration) {
	this.distance = distance;
	this.duration = duration;
    }
    
    public double getDuration() {
        return duration;
    }

    public double getDistance() {
        return distance;
    }
    
}
