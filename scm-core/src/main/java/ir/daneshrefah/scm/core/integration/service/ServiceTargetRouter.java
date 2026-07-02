package ir.daneshrefah.scm.core.integration.service;

import ir.daneshrefah.scm.common.model.gateway.Service;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRouteContext;
import ir.daneshrefah.scm.core.integration.service.routing.ServiceTargetRoutingRegistry;
import org.apache.camel.model.RouteDefinition;
import org.springframework.stereotype.Component;

@Component
public class ServiceTargetRouter {
    private final ServiceTargetRoutingRegistry registry;

    public ServiceTargetRouter(ServiceTargetRoutingRegistry registry) {
        this.registry = registry;
    }

    public void buildTarget(RouteDefinition route, Service service) {
        registry.getRequired(service.getRoutingStrategy())
                .buildTarget(new ServiceTargetRouteContext(route, service));
    }
}
