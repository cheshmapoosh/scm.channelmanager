package ir.daneshrefah.scm.provider.task.workflow;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

public record TaskWorkflowExecutionSnapshot(
        int schemaVersion,
        String executionId,
        String serviceCode,
        String inboundAction,
        String actionPlanName,
        String gatewayServiceVersion,
        String definitionId,
        String planFingerprint,
        String routingStrategy,
        TaskWorkflowExecutionState executionState,
        TaskWorkflowExecutionDecision decision,
        Long processId,
        List<TaskWorkflowStepSnapshot> steps,
        TaskWorkflowResumeData resumeData,
        TaskWorkflowStoredResponse storedResponse,
        TaskWorkflowStoredFailure storedFailure,
        String activeAttemptId,
        String activeStepId,
        Integer activeStepIndex,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int CURRENT_SCHEMA_VERSION = 1;

    public TaskWorkflowExecutionSnapshot {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        Objects.requireNonNull(executionId, "executionId must not be null");
        Objects.requireNonNull(serviceCode, "serviceCode must not be null");
        Objects.requireNonNull(inboundAction, "inboundAction must not be null");
        Objects.requireNonNull(actionPlanName, "actionPlanName must not be null");
        Objects.requireNonNull(
                gatewayServiceVersion,
                "gatewayServiceVersion must not be null"
        );
        Objects.requireNonNull(definitionId, "definitionId must not be null");
        Objects.requireNonNull(planFingerprint, "planFingerprint must not be null");
        Objects.requireNonNull(routingStrategy, "routingStrategy must not be null");
        Objects.requireNonNull(executionState, "executionState must not be null");
        steps = List.copyOf(Objects.requireNonNull(steps, "steps must not be null"));
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
    }

    public TaskWorkflowExecutionSnapshot withoutActiveAttempt() {
        return new TaskWorkflowExecutionSnapshot(
                schemaVersion,
                executionId,
                serviceCode,
                inboundAction,
                actionPlanName,
                gatewayServiceVersion,
                definitionId,
                planFingerprint,
                routingStrategy,
                executionState,
                decision,
                processId,
                steps,
                resumeData,
                storedResponse,
                storedFailure,
                null,
                null,
                null,
                createdAt,
                updatedAt
        );
    }
}
