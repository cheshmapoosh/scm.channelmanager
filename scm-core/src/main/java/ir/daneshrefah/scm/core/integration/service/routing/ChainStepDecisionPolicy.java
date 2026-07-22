package ir.daneshrefah.scm.core.integration.service.routing;

public interface ChainStepDecisionPolicy {
    String code();

    ChainStepDecision decide(ChainStepDecisionContext context);

    default String normalizedOutcome(ChainStepDecisionContext context, ChainStepDecision decision) {
        return null;
    }
}
