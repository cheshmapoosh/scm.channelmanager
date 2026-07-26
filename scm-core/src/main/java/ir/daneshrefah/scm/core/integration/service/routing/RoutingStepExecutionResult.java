package ir.daneshrefah.scm.core.integration.service.routing;

public record RoutingStepExecutionResult(
        Object response,
        Throwable failure,
        long elapsedMs,
        RoutingDecisionResult decisionResult
) {
    public RoutingDecision decision() {
        return decisionResult.decision();
    }

    public String normalizedOutcome() {
        return decisionResult.normalizedOutcome();
    }
}
