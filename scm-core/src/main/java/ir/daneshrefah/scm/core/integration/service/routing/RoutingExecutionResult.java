package ir.daneshrefah.scm.core.integration.service.routing;

public record RoutingExecutionResult(
        Object response,
        RoutingExecutionContext context,
        RoutingDecision decision,
        RoutingStepPlan stoppedAt
) {
    public RoutingExecutionResult {
        if (decision == RoutingDecision.FAIL) {
            throw new IllegalArgumentException(
                    "RoutingExecutionResult must not represent FAIL");
        }
    }
}
