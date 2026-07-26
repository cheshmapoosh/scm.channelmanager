package ir.daneshrefah.scm.core.integration.service.routing;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

public record RoutingFailureDetails(
        RoutingPlanIdentity planIdentity,
        String planId,
        String stepId,
        int stepIndex,
        String operationName,
        RoutingDecision decision,
        MessageStatus messageStatus,
        String reasonCode,
        String reasonMessage,
        String normalizedOutcome
) {
}
