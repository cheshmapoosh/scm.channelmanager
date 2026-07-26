package ir.daneshrefah.scm.core.integration.service.routing;

import org.apache.camel.Exchange;

public interface RoutingExecutionLifecycle {
    RoutingExecutionLifecycle NOOP = new RoutingExecutionLifecycle() {
    };

    default void beforeStep(
            Exchange exchange,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingExecutionContext context
    ) {
    }

    default void afterStep(
            Exchange exchange,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            RoutingStepExecutionResult result
    ) {
    }

    default void afterExecution(
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionResult result
    ) {
    }

    default void stepThrew(
            Exchange exchange,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            RuntimeException failure
    ) {
    }
}
