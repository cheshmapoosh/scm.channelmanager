package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.ExecutionOutcome;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionLifecycle;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureDetails;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowAttemptConflictException;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionDecision;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionSnapshot;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionState;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRecoveryStore;
import org.apache.camel.Exchange;

import java.time.Instant;
import java.util.UUID;

/**
 * Applies persistence and canonical-response side effects around routing-engine
 * step execution. Routing engines remain responsible for ordering and response
 * selection.
 */
final class TaskWorkflowExecutionLifecycle
        implements RoutingExecutionLifecycle {

    private final TaskWorkflowRecoveryStore store;
    private final TaskWorkflowCommandPlan commandPlan;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowSnapshotMapper snapshotMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;

    private TaskWorkflowExecutionSnapshot snapshot;
    private Message response;
    private boolean attemptPersisted;

    TaskWorkflowExecutionLifecycle(
            TaskWorkflowRecoveryStore store,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionSnapshot snapshot,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowSnapshotMapper snapshotMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator
    ) {
        this.store = store;
        this.commandPlan = commandPlan;
        this.snapshot = snapshot;
        this.payloadMapper = payloadMapper;
        this.snapshotMapper = snapshotMapper;
        this.transactionCoordinator = transactionCoordinator;
    }

    Message response() {
        return response;
    }

    @Override
    public void beforeStep(
            Exchange exchange,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingExecutionContext context
    ) {
        String attemptId = UUID.randomUUID().toString();
        String providerIdempotencyKey =
                snapshot.execution().executionId() + ":" + step.stepId();
        exchange.setProperty(
                TaskWorkflowExchangeProperties.PROVIDER_IDEMPOTENCY_KEY,
                providerIdempotencyKey
        );
        TaskWorkflowExecutionSnapshot attempted = snapshotMapper.attempted(
                snapshot,
                step,
                context,
                attemptId,
                Instant.now()
        );
        if (attempted.execution().processId() != null) {
            try {
                attempted = store.registerAttempt(
                        attempted.execution().processId(),
                        attempted,
                        snapshot.status().activeAttemptId()
                );
            } catch (RuntimeException failure) {
                throw persistenceFailure("register an attempt", failure);
            }
            attemptPersisted = true;
        } else {
            attemptPersisted = false;
        }
        snapshot = attempted;
        transactionCoordinator.beforeStep(exchange, step);
    }

    @Override
    public void afterStep(
            Exchange exchange,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            RoutingStepExecutionResult result
    ) {
        if (result.decision() == RoutingDecision.SUCCESS) {
            payloadMapper.rememberStepContext(
                    exchange,
                    step.observationContext().taskWorkflowStepType(),
                    result.response(),
                    context
            );
        }

        boolean lastStep = step.stepIndex() == plan.steps().size() - 1;
        boolean failed = result.decision() == RoutingDecision.FAIL;
        TaskWorkflowExecutionState state = failed
                ? TaskWorkflowExecutionState.FAILED
                : TaskWorkflowExecutionState.RUNNING;
        RoutingFailureDetails failureDetails = failed
                ? failureDetails(plan, step, result.decisionResult())
                : null;

        TaskWorkflowExecutionSnapshot updated = snapshotMapper.decided(
                snapshot,
                step,
                result,
                context,
                state,
                failed ? RoutingDecision.FAIL : null,
                failureDetails == null
                        ? null
                        : snapshotMapper.storeFailure(failureDetails),
                exchange.getProperty(
                        TaskWorkflowExchangeProperties.CURRENT_STEP_REQUEST
                ),
                Instant.now()
        );
        updated = registerAttemptWhenProcessBecomesVisible(updated);
        if (failed) {
            persistFailed(updated);
        } else if (result.decision() == RoutingDecision.SUCCESS
                && !lastStep) {
            persistProgress(updated);
        }
        snapshot = updated;
        transactionCoordinator.afterStep(exchange, step, result);
    }

    private TaskWorkflowExecutionSnapshot registerAttemptWhenProcessBecomesVisible(
            TaskWorkflowExecutionSnapshot updated
    ) {
        if (attemptPersisted || updated.execution().processId() == null) {
            return updated;
        }
        try {
            TaskWorkflowExecutionSnapshot registered = store.registerAttempt(
                    updated.execution().processId(),
                    updated,
                    null
            );
            attemptPersisted = true;
            return registered;
        } catch (RuntimeException failure) {
            throw persistenceFailure(
                    "register an attempt for the newly created process",
                    failure
            );
        }
    }

    @Override
    public void afterExecution(
            Exchange exchange,
            RoutingPlan plan,
            RoutingExecutionResult result
    ) {
        if (result.decision() == RoutingDecision.FAIL) {
            throw new IllegalStateException(
                    "Routing engine returned a normal FAIL result"
            );
        }
        String reasonCode = terminalReasonCode(result);
        Message canonical = canonicalResponse(
                exchange,
                result,
                result.response(),
                reasonCode
        );
        TaskWorkflowExecutionState state =
                result.decision() == RoutingDecision.RETRY_LATER
                        ? TaskWorkflowExecutionState.RETRY_PENDING
                        : TaskWorkflowExecutionState.COMPLETED;
        TaskWorkflowExecutionSnapshot terminal = snapshotMapper.terminal(
                snapshot,
                result.context(),
                state,
                result.decision(),
                snapshotMapper.storeResponse(canonical),
                Instant.now()
        );
        persistTerminal(terminal);
        snapshot = terminal;
        response = canonical;
    }

    @Override
    public void stepThrew(
            Exchange exchange,
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingExecutionContext context,
            RuntimeException failure
    ) {
        RoutingFailureDetails details = new RoutingFailureDetails(
                plan.identity(),
                plan.planId(),
                step.stepId(),
                step.stepIndex(),
                step.serviceOperation().getOperationName(),
                RoutingDecision.FAIL,
                MessageStatus.SC_ERROR_SYSTEM,
                "ROUTING_STEP_EXECUTION_ERROR",
                "Routing step execution failed",
                null
        );
        TaskWorkflowExecutionSnapshot failed = snapshotMapper.failed(
                snapshot,
                step,
                context,
                details,
                Instant.now()
        );
        if (failed.execution().processId() != null) {
            try {
                store.saveFailed(failed.execution().processId(), failed);
            } catch (RuntimeException persistenceFailure) {
                throw persistenceFailure(
                        "save a failed execution",
                        persistenceFailure
                );
            }
        }
        snapshot = failed;
    }

    private Message canonicalResponse(
            Exchange exchange,
            RoutingExecutionResult result,
            Object selectedResponse,
            String reasonCode
    ) {
        RoutingDecision decision = result.decision();
        MessageStatus status = decision == RoutingDecision.RETRY_LATER
                ? MessageStatus.SC_PROCESSING
                : MessageStatus.SC_SUCCESS;
        ExecutionOutcome outcome = new ExecutionOutcome(
                result.context().processId() == null
                        ? null
                        : snapshot.execution().executionId(),
                snapshot.plan().serviceCode(),
                snapshot.plan().inboundAction(),
                snapshot.plan().actionPlanName(),
                snapshot.execution().gatewayServiceVersion(),
                commandPlan.routingPlan().routingStrategy().name(),
                decision.name(),
                decision == RoutingDecision.RETRY_LATER,
                reasonCode
        );
        if (selectedResponse instanceof Message message) {
            message.status(status);
            return message.executionOutcome(outcome);
        }
        return Message.builder()
                .status(status)
                .payload(snapshotMapper.toJsonNode(selectedResponse))
                .executionOutcome(outcome)
                .build();
    }

    private void persistProgress(TaskWorkflowExecutionSnapshot progress) {
        Long processId = progress.execution().processId();
        if (processId == null) {
            return;
        }
        try {
            store.saveProgress(processId, progress);
        } catch (RuntimeException failure) {
            throw persistenceFailure("save execution progress", failure);
        }
    }

    private void persistFailed(TaskWorkflowExecutionSnapshot failed) {
        Long processId = failed.execution().processId();
        if (processId == null) {
            return;
        }
        try {
            store.saveFailed(processId, failed);
        } catch (RuntimeException failure) {
            throw persistenceFailure("save a failed execution", failure);
        }
    }

    private void persistTerminal(TaskWorkflowExecutionSnapshot terminal) {
        Long processId = terminal.execution().processId();
        if (processId == null) {
            if (terminal.status().decision()
                    == TaskWorkflowExecutionDecision.RETRY_LATER) {
                throw new TaskWorkflowPersistenceException(
                        "Cannot return RETRY_LATER without a workflow "
                                + "processId for durable state persistence"
                );
            }
            return;
        }
        try {
            if (terminal.status().decision()
                    == TaskWorkflowExecutionDecision.RETRY_LATER) {
                store.saveRetryLater(processId, terminal);
            } else {
                store.saveCompleted(processId, terminal);
            }
        } catch (RuntimeException failure) {
            throw persistenceFailure(
                    "save a terminal execution outcome",
                    failure
            );
        }
    }

    private String terminalReasonCode(RoutingExecutionResult result) {
        RoutingStepPlan stoppedAt = result.stoppedAt();
        int index = stoppedAt == null
                ? snapshot.steps().size() - 1
                : stoppedAt.stepIndex();
        if (index < 0 || index >= snapshot.steps().size()) {
            return null;
        }
        return snapshot.steps().get(index).reasonCode();
    }

    private RoutingFailureDetails failureDetails(
            RoutingPlan plan,
            RoutingStepPlan step,
            RoutingDecisionResult decision
    ) {
        return new RoutingFailureDetails(
                plan.identity(),
                plan.planId(),
                step.stepId(),
                step.stepIndex(),
                step.serviceOperation().getOperationName(),
                RoutingDecision.FAIL,
                decision.messageStatus(),
                decision.reasonCode(),
                decision.reasonMessage(),
                decision.normalizedOutcome()
        );
    }

    private RuntimeException persistenceFailure(
            String action,
            RuntimeException failure
    ) {
        if (failure instanceof TaskWorkflowExecutionException) {
            return failure;
        }
        if (failure instanceof TaskWorkflowAttemptConflictException) {
            return new TaskWorkflowExecutionAlreadyInProgressException(
                    "provider-attempt"
            );
        }
        return new TaskWorkflowPersistenceException(
                "Failed to " + action
                        + " TASK_WORKFLOW durable execution state",
                failure
        );
    }
}
