package ir.daneshrefah.scm.core.config;

import org.apache.camel.*;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.spi.LifecycleStrategy;

import java.util.Collection;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2020-07-06
 */
public class CamelLifecycleStrategy implements LifecycleStrategy {
    @Override
    public void onComponentAdd(String name, Component component) {

    }

    @Override
    public void onComponentRemove(String name, Component component) {

    }

    @Override
    public void onEndpointAdd(Endpoint endpoint) {

    }

    @Override
    public void onEndpointRemove(Endpoint endpoint) {

    }

    @Override
    public void onServiceAdd(CamelContext context, Service service, Route route) {

    }

    @Override
    public void onServiceRemove(CamelContext context, Service service, Route route) {

    }

    @Override
    public void onRoutesAdd(Collection<Route> routes) {
        for (Route route : routes) {
            try {
//                route.getCamelContext().addRoutes(new AdviceWithRouteBuilder() {
//                    @Override
//                    public void configure() throws Exception {
//                        String routeId = route.getId();
//                        weaveById(routeId)
//                                .before()
//                                .process(exchange -> {
////                                    TODO
//                                    System.out.println("Processing exchange in " + route.getId());
//                                });
//                    }
//                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onRoutesRemove(Collection<Route> routes) {

    }

    @Override
    public void onRouteContextCreate(Route route) {

    }

    @Override
    public void onThreadPoolAdd(CamelContext camelContext, ThreadPoolExecutor threadPool, String id, String sourceId, String routeId, String threadPoolProfileId) {

    }

    @Override
    public void onThreadPoolRemove(CamelContext camelContext, ThreadPoolExecutor threadPool) {

    }
}
