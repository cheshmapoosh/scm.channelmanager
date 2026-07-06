package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.ChannelServiceDefinition;
import ir.daneshrefah.scm.common.model.gateway.Service;
import org.apache.camel.model.RouteDefinition;

import java.util.List;

public record ServiceTargetRouteContext(
        RouteDefinition route,
        Service service,
        List<ChannelServiceDefinition> routeDefinitions
) {
    public ServiceTargetRouteContext(RouteDefinition route, Service service) {
        this(route, service, List.of());
    }

    public ServiceTargetRouteContext {
        routeDefinitions = routeDefinitions == null ? List.of() : List.copyOf(routeDefinitions);
    }
}
