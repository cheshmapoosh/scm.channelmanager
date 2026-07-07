package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;

public interface ServiceTargetRoutingHandler {
    RoutingStrategy strategy();

    void buildTarget(ServiceTargetRouteContext context);
}
