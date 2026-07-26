package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;

import java.time.Instant;

public record RoutingStepSnapshot(
        String stepId,
        int stepIndex,
        TaskWorkflowStepType stepType,
        String operationName,
        RoutingDecision decision,
        int attemptCount,
        String attemptId,
        String normalizedOutcome,
        String reasonCode,
        String reasonMessage,
        MessageStatus messageStatus,
        Instant attemptedAt,
        Instant updatedAt
) {
}
