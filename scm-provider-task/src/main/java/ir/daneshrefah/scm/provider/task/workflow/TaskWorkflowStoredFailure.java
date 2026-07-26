package ir.daneshrefah.scm.provider.task.workflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Safe, versionable failure metadata. Throwable instances and stack traces are
 * deliberately excluded.
 */
public record TaskWorkflowStoredFailure(
        String serviceCode,
        String inboundAction,
        String actionPlanName,
        String definitionId,
        String planFingerprint,
        String planId,
        String stepId,
        int stepIndex,
        String operationName,
        MessageStatus messageStatus,
        String reasonCode,
        String reasonMessage,
        String normalizedOutcome
) {
}
