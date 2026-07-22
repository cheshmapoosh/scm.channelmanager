package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import org.apache.camel.Exchange;

public interface RoutingEngine {
    RoutingStrategy strategy();

    RoutingExecutionResult execute(Exchange exchange, RoutingPlan plan);
}
