package routing;

import org.apache.commons.pool2.BasePooledObjectFactory;
import org.apache.commons.pool2.PooledObject;
import org.apache.commons.pool2.impl.DefaultPooledObject;

import networkmodel.RoadNetworkModel;

/**
 * This is creates a pool of routing objects for parallelized routing service.
 * 
 * @author abhinav.sunderrajan
 *
 */
public class RoutingPoolFactory extends BasePooledObjectFactory<Djikstra> {

    private RoadNetworkModel rnwModel;

    /**
     * Initialize the routing instances with the road network model.
     * 
     * @param rnwModel
     */
    public RoutingPoolFactory(RoadNetworkModel rnwModel) {
	this.rnwModel = rnwModel;

    }

    @Override
    public Djikstra create() throws Exception {
	return new Djikstra(rnwModel);
    }

    @Override
    public PooledObject<Djikstra> wrap(Djikstra obj) {
	return new DefaultPooledObject<Djikstra>(obj);
    }

    @Override
    public void passivateObject(PooledObject<Djikstra> pooledObject) {
	pooledObject.getObject().reset();
    }

}
