package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Versioned, provider-owned durable representation of one task-workflow
 * execution. Routing definitions are deliberately not duplicated here; the
 * current plan is validated by its identity and fingerprint before recovery.
 */
public record TaskWorkflowExecutionSnapshot(
        int schemaVersion,
        ExecutionIdentity execution,
        PlanIdentity plan,
        ExecutionStatus status,
        List<StepState> steps,
        ResumeData resume,
        TerminalOutcome terminal,
        Instant createdAt,
        Instant updatedAt
) {
    public static final int CURRENT_SCHEMA_VERSION = 2;

    public TaskWorkflowExecutionSnapshot {
        if (schemaVersion <= 0) {
            throw new IllegalArgumentException("schemaVersion must be positive");
        }
        Objects.requireNonNull(execution, "execution must not be null");
        Objects.requireNonNull(plan, "plan must not be null");
        Objects.requireNonNull(status, "status must not be null");
        steps = List.copyOf(Objects.requireNonNull(
                steps,
                "steps must not be null"
        ));
        Objects.requireNonNull(createdAt, "createdAt must not be null");
        Objects.requireNonNull(updatedAt, "updatedAt must not be null");
        Integer activeStepIndex = status.activeStepIndex();
        if (activeStepIndex != null
                && (activeStepIndex < 0 || activeStepIndex >= steps.size())) {
            throw new IllegalArgumentException(
                    "activeStepIndex is outside the persisted step range"
            );
        }
        validateTerminal(status, terminal);
    }

    public TaskWorkflowExecutionSnapshot withoutActiveAttempt() {
        return new TaskWorkflowExecutionSnapshot(
                schemaVersion,
                execution,
                plan,
                new ExecutionStatus(
                        status.state(),
                        status.decision(),
                        null,
                        null
                ),
                steps,
                resume,
                terminal,
                createdAt,
                updatedAt
        );
    }

    private static void validateTerminal(
            ExecutionStatus status,
            TerminalOutcome terminal
    ) {
        if (status.state() == TaskWorkflowExecutionState.RUNNING) {
            if (terminal != null) {
                throw new IllegalArgumentException(
                        "RUNNING execution must not contain a terminal outcome"
                );
            }
            return;
        }
        if (status.state() == TaskWorkflowExecutionState.COMPLETED
                && status.decision()
                == TaskWorkflowExecutionDecision.SUCCESS) {
            requireResponseOnly(terminal, "COMPLETED/SUCCESS");
            return;
        }
        if (status.state() == TaskWorkflowExecutionState.RETRY_PENDING
                && status.decision()
                == TaskWorkflowExecutionDecision.RETRY_LATER) {
            requireResponseOnly(terminal, "RETRY_PENDING/RETRY_LATER");
            return;
        }
        if (status.state() == TaskWorkflowExecutionState.FAILED
                && status.decision()
                == TaskWorkflowExecutionDecision.FAIL) {
            if (terminal == null
                    || terminal.failure() == null
                    || terminal.response() != null) {
                throw new IllegalArgumentException(
                        "FAILED/FAIL execution requires failure only"
                );
            }
            return;
        }
        throw new IllegalArgumentException(
                "Inconsistent task-workflow execution state and decision"
        );
    }

    private static void requireResponseOnly(
            TerminalOutcome terminal,
            String state
    ) {
        if (terminal == null
                || terminal.response() == null
                || terminal.failure() != null) {
            throw new IllegalArgumentException(
                    state + " execution requires response only"
            );
        }
    }

    public record ExecutionIdentity(
            String executionId,
            Long processId,
            String gatewayServiceVersion
    ) {
        public ExecutionIdentity {
            Objects.requireNonNull(
                    executionId,
                    "executionId must not be null"
            );
            Objects.requireNonNull(
                    gatewayServiceVersion,
                    "gatewayServiceVersion must not be null"
            );
        }
    }

    public record PlanIdentity(
            String serviceCode,
            String inboundAction,
            String actionPlanName,
            String definitionId,
            String fingerprint
    ) {
        public PlanIdentity {
            Objects.requireNonNull(serviceCode, "serviceCode must not be null");
            Objects.requireNonNull(
                    inboundAction,
                    "inboundAction must not be null"
            );
            Objects.requireNonNull(
                    actionPlanName,
                    "actionPlanName must not be null"
            );
            Objects.requireNonNull(
                    definitionId,
                    "definitionId must not be null"
            );
            Objects.requireNonNull(
                    fingerprint,
                    "fingerprint must not be null"
            );
        }
    }

    public record ExecutionStatus(
            TaskWorkflowExecutionState state,
            TaskWorkflowExecutionDecision decision,
            String activeAttemptId,
            Integer activeStepIndex
    ) {
        public ExecutionStatus {
            Objects.requireNonNull(state, "state must not be null");
            if ((activeAttemptId == null) != (activeStepIndex == null)) {
                throw new IllegalArgumentException(
                        "activeAttemptId and activeStepIndex must be set together"
                );
            }
        }
    }

    public record StepState(
            String stepId,
            int stepIndex,
            TaskWorkflowExecutionDecision decision,
            int attemptCount,
            String normalizedOutcome,
            String reasonCode,
            MessageStatus messageStatus,
            Instant attemptedAt
    ) {
        public StepState {
            Objects.requireNonNull(stepId, "stepId must not be null");
            if (stepIndex < 0) {
                throw new IllegalArgumentException(
                        "stepIndex must not be negative"
                );
            }
            if (attemptCount < 0) {
                throw new IllegalArgumentException(
                        "attemptCount must not be negative"
                );
            }
        }
    }

    public record ResumeData(
            JsonNode transactionData,
            JsonNode retryRequest,
            JsonNode lastBusinessResponse
    ) {
        public ResumeData {
            transactionData = copy(transactionData);
            retryRequest = copy(retryRequest);
            lastBusinessResponse = copy(lastBusinessResponse);
        }
    }

    public record TerminalOutcome(
            StoredResponse response,
            StoredFailure failure
    ) {
        public TerminalOutcome {
            if (response != null && failure != null) {
                throw new IllegalArgumentException(
                        "terminal outcome cannot contain response and failure"
                );
            }
        }
    }

    public record StoredResponse(
            MessageStatus status,
            JsonNode payload
    ) {
        public StoredResponse {
            Objects.requireNonNull(status, "status must not be null");
            payload = copy(payload);
        }
    }

    public record StoredFailure(
            Integer stepIndex,
            MessageStatus messageStatus,
            String reasonCode,
            String reasonMessage,
            String normalizedOutcome
    ) {
    }

    private static JsonNode copy(JsonNode value) {
        return value == null ? null : value.deepCopy();
    }
}
