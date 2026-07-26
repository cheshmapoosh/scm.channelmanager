package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.common.model.taskworkflow.ExecutionOutcome;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingCursor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecisionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingEngineRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionLifecycle;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureDetails;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureException;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingStepPlan;
import lombok.RequiredArgsConstructor;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

@Component
@RequiredArgsConstructor
public class TaskWorkflowExecutionCoordinator {
    private final ObjectProvider<TaskWorkflowRecoveryStore> storeProvider;
    private final RoutingEngineRegistry engineRegistry;
    private final RoutingRecoveryPolicyRegistry recoveryPolicyRegistry;
    private final TaskWorkflowExecutionIdentityResolver executionIdentityResolver;
    private final TaskWorkflowInputResolver inputResolver;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowSnapshotMapper snapshotMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;

    public void verifyDurableStoreAvailable() {
        requiredStore();
    }

    public RoutingExecutionResult execute(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan
    ) {
        TaskWorkflowRecoveryStore store = requiredStore();
        RoutingPlan plan = commandPlan.routingPlan();
        String gatewayServiceVersion = requireGatewayServiceVersion(exchange);
        TaskWorkflowExecutionIdentityResolver.ResolvedExecutionIdentity execution =
                executionIdentityResolver.resolve(exchange, commandPlan.command());
        Object originalRequest = exchange.getMessage().getBody();
        Long requestedProcessId = initialProcessId(exchange, plan, store);

        ExistingExecution existing = resolveExistingExecution(
                store,
                commandPlan,
                execution,
                requestedProcessId,
                gatewayServiceVersion
        );
        if (existing.snapshot() != null) {
            validateSnapshot(
                    existing.snapshot(),
                    execution.executionId(),
                    plan,
                    gatewayServiceVersion
            );
            RoutingExecutionResult terminal = terminalResult(
                    originalRequest,
                    existing.snapshot()
            );
            if (terminal != null) {
                return terminal;
            }
        }

        RoutingExecutionContext context;
        RoutingExecutionSnapshot snapshot;
        RoutingCursor cursor;
        if (existing.snapshot() == null) {
            context = new RoutingExecutionContext(originalRequest);
            context.processId(existing.processId());
            context.correlationId(execution.executionId());
            snapshot = snapshotMapper.initial(
                    execution.executionId(),
                    plan,
                    gatewayServiceVersion,
                    existing.processId(),
                    context,
                    Instant.now()
            );
            cursor = RoutingCursor.start(plan);
        } else {
            snapshot = existing.snapshot();
            context = snapshotMapper.restoreContext(
                    originalRequest,
                    snapshot
            );
            cursor = recoveryPolicyRegistry
                    .getRequired(plan.routingStrategy())
                    .resolveRetryCursor(plan, snapshot);
        }

        exchange.setProperty(Message.EXECUTION_ID, execution.executionId());
        if (context.processId() != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    context.processId()
            );
        }
        AtomicReference<RoutingExecutionSnapshot> snapshotRef =
                new AtomicReference<>(snapshot);
        AtomicReference<Message> responseRef = new AtomicReference<>();
        SnapshotLifecycle lifecycle = new SnapshotLifecycle(
                store,
                commandPlan,
                gatewayServiceVersion,
                snapshotRef,
                responseRef
        );

        RoutingExecutionResult engineResult = engineRegistry
                .getRequired(plan.routingStrategy())
                .execute(exchange, plan, context, cursor, lifecycle);
        Message response = responseRef.get();
        if (response == null) {
            response = canonicalResponse(
                    exchange,
                    commandPlan,
                    gatewayServiceVersion,
                    engineResult,
                    engineResult.response(),
                    null
            );
        }
        return new RoutingExecutionResult(
                response,
                engineResult.context(),
                engineResult.decision(),
                engineResult.stoppedAt()
        );
    }

    private ExistingExecution resolveExistingExecution(
            TaskWorkflowRecoveryStore store,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionIdentityResolver.ResolvedExecutionIdentity execution,
            Long requestedProcessId,
            String gatewayServiceVersion
    ) {
        if (!execution.explicitlySupplied()) {
            return new ExistingExecution(requestedProcessId, null);
        }

        OptionalLong correlatedProcess =
                store.findProcessIdByExecutionId(execution.executionId());
        if (requestedProcessId != null && correlatedProcess.isPresent()
                && requestedProcessId.longValue()
                != correlatedProcess.getAsLong()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW executionId resolves to processId="
                            + correlatedProcess.getAsLong()
                            + " but the request supplied processId="
                            + requestedProcessId);
        }
        Long processId = requestedProcessId != null
                ? requestedProcessId
                : correlatedProcess.isPresent()
                ? correlatedProcess.getAsLong()
                : null;

        if (processId == null
                && commandPlan.command() == TaskWorkflowCommand.START) {
            return new ExistingExecution(null, null);
        }
        if (processId == null) {
            throw unavailable(execution.executionId(), commandPlan,
                    gatewayServiceVersion,
                    "process identity is unavailable");
        }

        Optional<RoutingExecutionSnapshot> loaded = store.loadForUpdate(
                processId,
                commandPlan.inboundAction(),
                gatewayServiceVersion
        );
        if (loaded.isEmpty()) {
            throw unavailable(execution.executionId(), commandPlan,
                    gatewayServiceVersion,
                    commandPlan.routingPlan().steps().stream()
                            .anyMatch(step -> step.observationContext()
                                    .taskWorkflowStepType()
                                    == TaskWorkflowStepType.BUSINESS_OPERATION)
                            ? "snapshot is missing; a business-operation chain must not be replayed blindly"
                            : "snapshot is missing; execution must not be restarted blindly");
        }
        return new ExistingExecution(processId, loaded.get());
    }

    private RoutingExecutionResult terminalResult(
            Object originalRequest,
            RoutingExecutionSnapshot snapshot
    ) {
        if (snapshot.decision() == RoutingDecision.SUCCESS) {
            if (snapshot.executionState() != RoutingExecutionState.COMPLETED) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "SUCCESS snapshot is not in COMPLETED state");
            }
            return new RoutingExecutionResult(
                    snapshotMapper.restoreResponse(snapshot.storedResponse()),
                    snapshotMapper.restoreContext(originalRequest, snapshot),
                    RoutingDecision.SUCCESS,
                    null
            );
        }
        if (snapshot.decision() == RoutingDecision.FAIL) {
            if (snapshot.executionState() != RoutingExecutionState.FAILED
                    || snapshot.storedFailure() == null) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "FAIL snapshot has no stored typed failure");
            }
            throw new RoutingFailureException(
                    snapshot.storedFailure(),
                    null
            );
        }
        if (snapshot.decision() == RoutingDecision.RETRY_LATER) {
            if (snapshot.executionState()
                    != RoutingExecutionState.RETRY_PENDING) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "RETRY_LATER snapshot is not in RETRY_PENDING state");
            }
            return null;
        }
        throw new TaskWorkflowAttemptConflictException(
                "TASK_WORKFLOW execution has an in-progress attempt and cannot be resumed");
    }

    private void validateSnapshot(
            RoutingExecutionSnapshot snapshot,
            String executionId,
            RoutingPlan plan,
            String gatewayServiceVersion
    ) {
        if (snapshot.schemaVersion()
                != RoutingExecutionSnapshot.CURRENT_SCHEMA_VERSION) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Unsupported TASK_WORKFLOW snapshot schemaVersion="
                            + snapshot.schemaVersion());
        }
        if (!executionId.equals(snapshot.executionId())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW executionId does not match the persisted snapshot");
        }
        if (!gatewayServiceVersion.equals(
                snapshot.gatewayServiceVersion())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW retry gateway service version does not "
                            + "match the persisted request contract");
        }
        if (!snapshot.planIdentity().equals(plan.identity())
                || snapshot.routingStrategy() != plan.routingStrategy()) {
            throw new TaskWorkflowPlanChangedException(
                    "TASK_WORKFLOW plan changed during retry; persisted="
                            + snapshot.planIdentity() + ", current="
                            + plan.identity());
        }
    }

    private Long initialProcessId(
            Exchange exchange,
            RoutingPlan plan,
            TaskWorkflowRecoveryStore store
    ) {
        TaskWorkflowStepType stepType = plan.steps().getFirst()
                .observationContext().taskWorkflowStepType();
        return switch (stepType) {
            case APPROVE_PROCESS, CANCEL_PROCESS,
                 FIND_TASK_BY_PROCESS_ID, UPDATE_PROCESS_DESCRIPTION,
                 COMPLETE_PROCESS ->
                    inputResolver.resolveProcessId(exchange, stepType);
            case COMPLETE_TASK -> {
                Long suppliedProcessId =
                        inputResolver.resolveProcessId(exchange, stepType);
                long taskId = inputResolver.requireTaskId(exchange, stepType);
                OptionalLong taskProcess = store.findProcessIdByTaskId(taskId);
                if (taskProcess.isEmpty()) {
                    throw new InvalidTaskWorkflowExecutionStateException(
                            "TASK_WORKFLOW taskId=" + taskId
                                    + " has no workflow process");
                }
                if (suppliedProcessId != null
                        && suppliedProcessId.longValue()
                        != taskProcess.getAsLong()) {
                    throw new InvalidTaskWorkflowExecutionStateException(
                            "Conflicting TASK_WORKFLOW processId values from "
                                    + "the request and taskId=" + taskId);
                }
                yield taskProcess.getAsLong();
            }
            default -> null;
        };
    }

    private Message canonicalResponse(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            String gatewayServiceVersion,
            RoutingExecutionResult result,
            Object selectedResponse,
            String reasonCode
    ) {
        RoutingDecision decision = result.decision();
        MessageStatus status = decision == RoutingDecision.RETRY_LATER
                ? MessageStatus.SC_PROCESSING
                : MessageStatus.SC_SUCCESS;
        ExecutionOutcome outcome = new ExecutionOutcome(
                exchange.getProperty(Message.EXECUTION_ID, String.class),
                commandPlan.routingPlan().identity().serviceCode(),
                commandPlan.inboundAction(),
                commandPlan.actionPlanName(),
                gatewayServiceVersion,
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

    private TaskWorkflowRecoveryStore requiredStore() {
        List<TaskWorkflowRecoveryStore> stores =
                storeProvider.orderedStream().toList();
        if (stores.size() != 1) {
            throw new IllegalStateException(
                    "Active TASK_WORKFLOW services require exactly one durable "
                            + "TaskWorkflowRecoveryStore; found " + stores.size());
        }
        return stores.getFirst();
    }

    private InvalidTaskWorkflowExecutionStateException unavailable(
            String executionId,
            TaskWorkflowCommandPlan plan,
            String gatewayServiceVersion,
            String reason
    ) {
        return new InvalidTaskWorkflowExecutionStateException(
                "TASK_WORKFLOW execution state is unavailable executionId="
                        + executionId + ", inboundAction="
                        + plan.inboundAction() + ", gatewayServiceVersion="
                        + gatewayServiceVersion + ": " + reason);
    }

    private String requireGatewayServiceVersion(Exchange exchange) {
        String gatewayServiceVersion = exchange.getProperty(
                Message.SERVICE_VERSION,
                String.class
        );
        if (gatewayServiceVersion == null
                || gatewayServiceVersion.isBlank()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW gateway service version is unavailable");
        }
        return gatewayServiceVersion;
    }

    private RuntimeException persistenceFailure(
            String action,
            RuntimeException failure
    ) {
        if (failure instanceof TaskWorkflowExecutionException) {
            return failure;
        }
        return new TaskWorkflowPersistenceException(
                "Failed to " + action
                        + " TASK_WORKFLOW durable execution state",
                failure
        );
    }

    private record ExistingExecution(
            Long processId,
            RoutingExecutionSnapshot snapshot
    ) {
    }

    private final class SnapshotLifecycle
            implements RoutingExecutionLifecycle {
        private final TaskWorkflowRecoveryStore store;
        private final TaskWorkflowCommandPlan commandPlan;
        private final String gatewayServiceVersion;
        private final AtomicReference<RoutingExecutionSnapshot> snapshotRef;
        private final AtomicReference<Message> responseRef;

        private SnapshotLifecycle(
                TaskWorkflowRecoveryStore store,
                TaskWorkflowCommandPlan commandPlan,
                String gatewayServiceVersion,
                AtomicReference<RoutingExecutionSnapshot> snapshotRef,
                AtomicReference<Message> responseRef
        ) {
            this.store = store;
            this.commandPlan = commandPlan;
            this.gatewayServiceVersion = gatewayServiceVersion;
            this.snapshotRef = snapshotRef;
            this.responseRef = responseRef;
        }

        @Override
        public void beforeStep(
                Exchange exchange,
                RoutingPlan plan,
                RoutingStepPlan step,
                RoutingExecutionContext context
        ) {
            RoutingExecutionSnapshot current = snapshotRef.get();
            String attemptId = UUID.randomUUID().toString();
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROVIDER_IDEMPOTENCY_KEY,
                    current.executionId() + ":" + step.stepId()
            );
            RoutingExecutionSnapshot attempted = snapshotMapper.attempted(
                    current,
                    step,
                    context,
                    attemptId,
                    Instant.now()
            );
            if (attempted.processId() != null) {
                try {
                    attempted = store.registerAttempt(
                            attempted.processId(),
                            attempted,
                            current.activeAttemptId()
                    );
                } catch (RuntimeException failure) {
                    throw persistenceFailure("register an attempt", failure);
                }
            }
            snapshotRef.set(attempted);
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
            RoutingExecutionState state = failed
                    ? RoutingExecutionState.FAILED
                    : RoutingExecutionState.RUNNING;
            RoutingDecision executionDecision = failed
                    ? RoutingDecision.FAIL
                    : null;
            RoutingFailureDetails failureDetails = failed
                    ? failureDetails(plan, step, result.decisionResult())
                    : null;

            RoutingExecutionSnapshot updated = snapshotMapper.decided(
                    snapshotRef.get(),
                    step,
                    result,
                    context,
                    state,
                    executionDecision,
                    null,
                    failureDetails,
                    Instant.now()
            );
            if (failed) {
                persistFailed(updated);
            } else if (result.decision() == RoutingDecision.SUCCESS
                    && !lastStep) {
                persistProgress(updated);
            }
            snapshotRef.set(updated);
            transactionCoordinator.afterStep(exchange, step, result);
        }

        @Override
        public void afterExecution(
                Exchange exchange,
                RoutingPlan plan,
                RoutingExecutionResult result
        ) {
            if (result.decision() == RoutingDecision.FAIL) {
                throw new IllegalStateException(
                        "Routing engine returned a normal FAIL result");
            }
            RoutingExecutionSnapshot current = snapshotRef.get();
            String reasonCode = terminalReasonCode(current, result);
            Message canonical = canonicalResponse(
                    exchange,
                    commandPlan,
                    gatewayServiceVersion,
                    result,
                    result.response(),
                    reasonCode
            );
            RoutingExecutionState state =
                    result.decision() == RoutingDecision.RETRY_LATER
                            ? RoutingExecutionState.RETRY_PENDING
                            : RoutingExecutionState.COMPLETED;
            RoutingExecutionSnapshot terminal = snapshotMapper.terminal(
                    current,
                    result.context(),
                    state,
                    result.decision(),
                    snapshotMapper.storeResponse(canonical),
                    Instant.now()
            );
            persistTerminal(terminal);
            snapshotRef.set(terminal);
            responseRef.set(canonical);
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
            RoutingExecutionSnapshot failed = snapshotMapper.failed(
                    snapshotRef.get(),
                    step,
                    context,
                    details,
                    Instant.now()
            );
            if (failed.processId() != null) {
                try {
                    store.saveFailed(failed.processId(), failed);
                } catch (RuntimeException persistenceFailure) {
                    throw persistenceFailure(
                            "save a failed execution",
                            persistenceFailure
                    );
                }
            }
            snapshotRef.set(failed);
        }

        private void persistProgress(RoutingExecutionSnapshot snapshot) {
            Long processId = snapshot.processId();
            if (processId == null) {
                return;
            }
            try {
                store.saveProgress(processId, snapshot);
            } catch (RuntimeException failure) {
                throw persistenceFailure("save execution progress", failure);
            }
        }

        private void persistFailed(RoutingExecutionSnapshot snapshot) {
            Long processId = snapshot.processId();
            if (processId == null) {
                return;
            }
            try {
                store.saveFailed(processId, snapshot);
            } catch (RuntimeException failure) {
                throw persistenceFailure("save a failed execution", failure);
            }
        }

        private void persistTerminal(RoutingExecutionSnapshot snapshot) {
            Long processId = snapshot.processId();
            if (processId == null) {
                if (snapshot.decision() == RoutingDecision.RETRY_LATER) {
                    throw new TaskWorkflowPersistenceException(
                            "Cannot return RETRY_LATER without a workflow "
                                    + "processId for durable state persistence");
                }
                return;
            }
            try {
                if (snapshot.decision() == RoutingDecision.RETRY_LATER) {
                    store.saveRetryLater(processId, snapshot);
                } else {
                    store.saveCompleted(processId, snapshot);
                }
            } catch (RuntimeException failure) {
                throw persistenceFailure(
                        "save a terminal execution outcome",
                        failure
                );
            }
        }

        private String terminalReasonCode(
                RoutingExecutionSnapshot snapshot,
                RoutingExecutionResult result
        ) {
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
    }
}
