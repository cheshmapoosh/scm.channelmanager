package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

import java.util.Objects;

public record RoutingDecisionResult(
        RoutingDecision decision,
        MessageStatus messageStatus,
        String reasonCode,
        String reasonMessage,
        String normalizedOutcome
) {
    public RoutingDecisionResult {
        Objects.requireNonNull(decision, "decision must not be null");
    }

    public RoutingDecisionResult withDecision(
            RoutingDecision replacement,
            MessageStatus replacementStatus,
            String replacementReasonCode,
            String replacementReason
    ) {
        return new RoutingDecisionResult(
                replacement,
                replacementStatus,
                replacementReasonCode,
                replacementReason,
                normalizedOutcome
        );
    }
}
