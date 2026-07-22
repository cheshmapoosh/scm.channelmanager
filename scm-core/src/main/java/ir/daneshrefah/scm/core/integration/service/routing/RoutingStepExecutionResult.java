package ir.daneshrefah.scm.core.integration.service.routing;

public record RoutingStepExecutionResult(
        Object response,
        Throwable failure,
        long elapsedMs,
        ChainStepDecision decision,
        String normalizedOutcome
) {
    public RoutingStepExecutionResult(Object response, Throwable failure, long elapsedMs) {
        this(
                response,
                failure,
                elapsedMs,
                failure == null ? ChainStepDecision.CONTINUE : ChainStepDecision.FAIL,
                null
        );
    }
}
