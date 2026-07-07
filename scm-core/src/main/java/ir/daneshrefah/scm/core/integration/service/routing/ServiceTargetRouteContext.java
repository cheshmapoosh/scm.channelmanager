package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.Service;
import org.apache.camel.model.RouteDefinition;

public record ServiceTargetRouteContext(
        RouteDefinition route,
        Service service
) {
}
