package ir.daneshrefah.scm.core.integration.service.routing;

public record RoutingExecutionResult(
        Object response,
        RoutingExecutionContext context,
        ChainStepDecision decision,
        RoutingStepPlan stoppedAt
) {
}
