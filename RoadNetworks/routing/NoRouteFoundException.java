package routing;

public class NoRouteFoundException extends RoutingException {

    public NoRouteFoundException(String message) {
	super(message);
    }
}
