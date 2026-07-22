package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.camel.Exchange;

public record ChainStepDecisionContext(
        Exchange exchange,
        RoutingStepPlan step,
        RoutingExecutionContext executionContext,
        Object response,
        Throwable failure
) {
}
