package routing;

import com.vividsolutions.jts.geom.Coordinate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import networkmodel.RoadNetworkModel;
import networkmodel.RoadNode;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Singleton class for multi threaded computation of routes backed by a thread executor and object
 * pooling. The route is returned as a {@link Future} array list.
 *
 * @author abhinav.sunderrajan
 */
public class RoutingServiceDjikstraMT {

  private ThreadPoolExecutor executor;
  private RoadNetworkModel rnwModel;
  private static GenericObjectPool<Djikstra> pool;
  private static RoutingServiceDjikstraMT router;

  private RoutingServiceDjikstraMT(RoadNetworkModel rnwModel, ThreadPoolExecutor executor) {
    this.rnwModel = rnwModel;
    this.executor = executor;
  }

  /**
   * Get and instance of the router. Think of this service as a routing provider.
   *
   * @param rnwModel the road network model
   * @param executor the thread pool executor for parallel routing operations
   * @return
   */
  public static RoutingServiceDjikstraMT getRouterInstance(
      RoadNetworkModel rnwModel, ThreadPoolExecutor executor) {
    if (router == null) {
      router = new RoutingServiceDjikstraMT(rnwModel, executor);
      pool = new GenericObjectPool<Djikstra>(new RoutingPoolFactory(rnwModel));
      pool.setMaxIdle(15);
    }

    return router;
  }

  /**
   * Route from the begin node to end node.
   *
   * @param beginNode the origin node
   * @param endNode the destination node.
   * @param criteria
   * @return the {@link CompletableFuture} object.
   */
  public CompletableFuture<JSONObject> getRoute(
      RoadNode beginNode, RoadNode endNode, Criteria criteria) {
    GetShortestPath getRoute = new GetShortestPath(beginNode, endNode, criteria, pool);
    return CompletableFuture.supplyAsync(getRoute, executor);
  }

  /**
   * Return the road network associated with the routing instance.
   *
   * @return road network model.
   */
  public RoadNetworkModel getRnwModel() {
    return rnwModel;
  }

  /**
   * Returns the route from the origin coordinate to destination coordinate.
   *
   * @param origin the origin coordinate.
   * @param destination the destination coordinate.
   * @param criteria the {@link Criteria} for routing.
   * @return the {@link JSONObject} which gives the route (a {@link JSONArray} of road ids),distance
   *     in meters and time of travel in seconds.
   */
  public synchronized JSONObject getRouteFromOriginToDestination(
      Coordinate origin, Coordinate destination, Criteria criteria) {
    try {
      JSONObject routeObject;
      List<RoadNode> closestOrigin = rnwModel.getNearestNodesSortedByDistance(origin);
      List<RoadNode> closestDest = rnwModel.getNearestNodesSortedByDistance(destination);

      for (RoadNode originNode : closestOrigin) {
        for (RoadNode destinationNode : closestDest) {
          if (originNode.getNodeId() == destinationNode.getNodeId()) {
            continue;
          } else {
            CompletableFuture<JSONObject> future = getRoute(originNode, destinationNode, criteria);
            routeObject = future.get();
            JSONArray route = routeObject.getJSONArray("route");
            if (route.length() > 0) {
              future.cancel(true);
              routeObject.put("origin_node", originNode.getNodeId());
              routeObject.put("dest_node", destinationNode.getNodeId());
              return routeObject;
            }
            future.cancel(true);
          }
        }
      }
      System.err.println(
          "Unable to find a route from the origin "
              + origin
              + " to destination "
              + destination
              + " coordinates ");
      return null;
    } catch (InterruptedException e) {
      e.printStackTrace();
    } catch (ExecutionException e) {
      e.printStackTrace();
    }
    return null;
  }

  /** Invalidate the instance to create a new one. */
  public static void invalidateInstance() {
    router = null;
  }
}
