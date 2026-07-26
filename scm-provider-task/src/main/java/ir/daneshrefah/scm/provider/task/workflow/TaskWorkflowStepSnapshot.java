package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;

import java.time.Instant;

public record TaskWorkflowStepSnapshot(
        String stepId,
        int stepIndex,
        TaskWorkflowStepType stepType,
        String operationName,
        TaskWorkflowExecutionDecision decision,
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
