package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import org.apache.camel.Exchange;

public interface RoutingEngine {
    RoutingStrategy strategy();

    default RoutingExecutionResult execute(Exchange exchange, RoutingPlan plan) {
        return execute(
                exchange,
                plan,
                new RoutingExecutionContext(exchange.getMessage().getBody()),
                RoutingCursor.start(plan),
                RoutingExecutionLifecycle.NOOP
        );
    }

    RoutingExecutionResult execute(
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionContext context,
            RoutingCursor cursor,
            RoutingExecutionLifecycle lifecycle
    );
}
