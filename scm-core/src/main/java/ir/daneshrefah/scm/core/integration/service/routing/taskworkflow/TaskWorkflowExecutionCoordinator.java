package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.gateway.RoutingStrategy;
import ir.daneshrefah.scm.common.model.message.Message;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingCursor;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingEngineRegistry;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionContext;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingExecutionResult;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingFailureException;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlan;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingPlanIdentity;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowAttemptConflictException;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionDecision;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionSnapshot;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowExecutionState;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowRecoveryStore;
import ir.daneshrefah.scm.provider.task.workflow.TaskWorkflowSnapshotTooLargeException;
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Coordinates durable task-workflow execution while routing engines remain
 * responsible only for ordered step execution.
 */
public class TaskWorkflowExecutionCoordinator {

    private final ObjectProvider<TaskWorkflowRecoveryStore> storeProvider;
    private final RoutingEngineRegistry engineRegistry;
    private final RoutingRecoveryPolicyRegistry recoveryPolicyRegistry;
    private final TaskWorkflowExecutionIdentityResolver executionIdentityResolver;
    private final TaskWorkflowInputResolver inputResolver;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowSnapshotMapper snapshotMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;
    private final TaskWorkflowDistributedLock distributedLock;

    public TaskWorkflowExecutionCoordinator(
            ObjectProvider<TaskWorkflowRecoveryStore> storeProvider,
            RoutingEngineRegistry engineRegistry,
            RoutingRecoveryPolicyRegistry recoveryPolicyRegistry,
            TaskWorkflowExecutionIdentityResolver executionIdentityResolver,
            TaskWorkflowInputResolver inputResolver,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowSnapshotMapper snapshotMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator,
            TaskWorkflowDistributedLock distributedLock
    ) {
        this.storeProvider = storeProvider;
        this.engineRegistry = engineRegistry;
        this.recoveryPolicyRegistry = recoveryPolicyRegistry;
        this.executionIdentityResolver = executionIdentityResolver;
        this.inputResolver = inputResolver;
        this.payloadMapper = payloadMapper;
        this.snapshotMapper = snapshotMapper;
        this.transactionCoordinator = transactionCoordinator;
        this.distributedLock = distributedLock;
    }

    public void verifyInfrastructureAvailable(String serviceCode) {
        requiredStore(serviceCode);
        distributedLock.verifyAvailable(serviceCode);
    }

    public RoutingExecutionResult execute(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        String serviceCode = plan.identity().serviceCode();
        TaskWorkflowRecoveryStore store = requiredStore(serviceCode);
        String gatewayServiceVersion = requireGatewayServiceVersion(exchange);
        TaskWorkflowExecutionIdentityResolver.ResolvedExecutionIdentity execution =
                executionIdentityResolver.resolve(
                        exchange,
                        commandPlan.command()
                );
        Long requestedProcessId = initialProcessId(exchange, plan, store);

        if (commandPlan.command() == TaskWorkflowCommand.START) {
            return distributedLock.withStartLock(
                    serviceCode,
                    execution.executionId(),
                    () -> executeUnderLock(
                            exchange,
                            commandPlan,
                            execution,
                            requestedProcessId,
                            gatewayServiceVersion,
                            store
                    )
            );
        }
        if (requestedProcessId != null) {
            return distributedLock.withProcessLock(
                    serviceCode,
                    requestedProcessId,
                    () -> executeUnderLock(
                            exchange,
                            commandPlan,
                            execution,
                            requestedProcessId,
                            gatewayServiceVersion,
                            store
                    )
            );
        }
        if (execution.explicitlySupplied()) {
            throw unavailable(
                    execution.executionId(),
                    commandPlan,
                    gatewayServiceVersion,
                    "retry requires processId or a taskId that resolves to a process"
            );
        }
        return executeUnderLock(
                exchange,
                commandPlan,
                execution,
                null,
                gatewayServiceVersion,
                store
        );
    }

    private RoutingExecutionResult executeUnderLock(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionIdentityResolver.ResolvedExecutionIdentity execution,
            Long requestedProcessId,
            String gatewayServiceVersion,
            TaskWorkflowRecoveryStore store
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        Object originalRequest = exchange.getMessage().getBody();
        ExistingExecution existing = resolveExistingExecution(
                store,
                commandPlan,
                execution,
                requestedProcessId,
                gatewayServiceVersion
        );
        if (existing.state() == ExistingExecutionState.EXISTING_SNAPSHOT) {
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
        TaskWorkflowExecutionSnapshot snapshot;
        RoutingCursor cursor;
        if (existing.state() != ExistingExecutionState.EXISTING_SNAPSHOT) {
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
        TaskWorkflowExecutionLifecycle lifecycle =
                new TaskWorkflowExecutionLifecycle(
                store,
                commandPlan,
                gatewayServiceVersion,
                snapshot,
                payloadMapper,
                snapshotMapper,
                transactionCoordinator
        );

        RoutingExecutionResult engineResult = engineRegistry
                .getRequired(plan.routingStrategy())
                .execute(exchange, plan, context, cursor, lifecycle);
        Message response = lifecycle.response();
        if (response == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW routing lifecycle did not produce a "
                            + "canonical response"
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
        if (commandPlan.command() == TaskWorkflowCommand.START) {
            if (!execution.explicitlySupplied()) {
                throw unavailable(
                        execution.executionId(),
                        commandPlan,
                        gatewayServiceVersion,
                        "START reconstruction requires an explicitly supplied "
                                + "canonical executionId"
                );
            }
            OptionalLong process = findCorrelatedProcess(
                    store,
                    execution.executionId()
            );
            if (process.isEmpty()) {
                return ExistingExecution.newExecution(null);
            }
            long processId = process.getAsLong();
            Optional<TaskWorkflowExecutionSnapshot> loaded =
                    load(store, processId);
            if (loaded.isEmpty()) {
                requireReconstructableStart(
                        commandPlan,
                        execution,
                        processId
                );
                return ExistingExecution.reconstructableStart(processId);
            }
            TaskWorkflowExecutionSnapshot snapshot = loaded.get();
            if (sameExecutionAction(
                    snapshot,
                    execution.executionId(),
                    commandPlan.inboundAction()
            )) {
                return ExistingExecution.existingSnapshot(
                        processId,
                        snapshot
                );
            }
            if (snapshot.executionState()
                    == TaskWorkflowExecutionState.RUNNING
                    || snapshot.executionState()
                    == TaskWorkflowExecutionState.RETRY_PENDING) {
                throw new TaskWorkflowExecutionAlreadyInProgressException(
                        "process:" + processId
                );
            }
            throw snapshotUnavailable(
                    execution.executionId(),
                    processId,
                    commandPlan,
                    "workflow watcher belongs to a different execution or "
                            + "inbound action; non-durable START replay is "
                            + "forbidden"
            );
        }

        if (requestedProcessId == null) {
            if (execution.explicitlySupplied()) {
                throw unavailable(
                        execution.executionId(),
                        commandPlan,
                        gatewayServiceVersion,
                        "process identity is unavailable"
                );
            }
            return ExistingExecution.newExecution(null);
        }
        Optional<TaskWorkflowExecutionSnapshot> loaded =
                load(store, requestedProcessId);
        if (loaded.isEmpty()) {
            throw snapshotUnavailable(
                    execution.executionId(),
                    requestedProcessId,
                    commandPlan,
                    commandPlan.routingPlan().steps().stream()
                            .anyMatch(step -> step.observationContext()
                                    .taskWorkflowStepType()
                                    == TaskWorkflowStepType.BUSINESS_OPERATION)
                            ? "workflow watcher is missing; a "
                            + "business-operation chain must not be replayed "
                            + "blindly"
                            : "workflow watcher is missing; execution must "
                            + "not be restarted blindly"
            );
        }
        TaskWorkflowExecutionSnapshot snapshot = loaded.get();
        if (execution.explicitlySupplied()) {
            return ExistingExecution.existingSnapshot(
                    requestedProcessId,
                    snapshot
            );
        }
        if (isTerminal(snapshot)) {
            return ExistingExecution.newExecution(requestedProcessId);
        }
        throw new TaskWorkflowExecutionAlreadyInProgressException(
                "process:" + requestedProcessId
        );
    }

    private Optional<TaskWorkflowExecutionSnapshot> load(
            TaskWorkflowRecoveryStore store,
            long processId
    ) {
        try {
            return store.loadForUpdate(processId);
        } catch (RuntimeException failure) {
            throw persistenceFailure("load execution state", failure);
        }
    }

    private OptionalLong findCorrelatedProcess(
            TaskWorkflowRecoveryStore store,
            String executionId
    ) {
        try {
            return store.findProcessIdByCorrelationId(executionId);
        } catch (RuntimeException failure) {
            throw persistenceFailure(
                    "resolve the exactly correlated process",
                    failure
            );
        }
    }

    private boolean sameExecutionAction(
            TaskWorkflowExecutionSnapshot snapshot,
            String executionId,
            String inboundAction
    ) {
        return executionId.equals(snapshot.executionId())
                && inboundAction.equalsIgnoreCase(snapshot.inboundAction());
    }

    private boolean isTerminal(TaskWorkflowExecutionSnapshot snapshot) {
        boolean completed = snapshot.executionState()
                == TaskWorkflowExecutionState.COMPLETED
                && snapshot.decision()
                == TaskWorkflowExecutionDecision.SUCCESS;
        boolean failed = snapshot.executionState()
                == TaskWorkflowExecutionState.FAILED
                && snapshot.decision()
                == TaskWorkflowExecutionDecision.FAIL;
        return completed || failed;
    }

    private void requireReconstructableStart(
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionIdentityResolver.ResolvedExecutionIdentity execution,
            long processId
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        boolean reconstructable = commandPlan.command()
                == TaskWorkflowCommand.START
                && execution.explicitlySupplied()
                && plan.routingStrategy() == RoutingStrategy.FIRST
                && plan.steps().size() == 1
                && plan.steps().getFirst().observationContext()
                .taskWorkflowStepType()
                == TaskWorkflowStepType.START_PROCESS;
        if (!reconstructable) {
            throw snapshotUnavailable(
                    execution.executionId(),
                    processId,
                    commandPlan,
                    "workflow watcher is missing and the action plan is not "
                            + "a one-step FIRST/START_PROCESS plan"
            );
        }
    }

    private RoutingExecutionResult terminalResult(
            Object originalRequest,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (snapshot.decision()
                == TaskWorkflowExecutionDecision.SUCCESS) {
            if (snapshot.executionState()
                    != TaskWorkflowExecutionState.COMPLETED) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "SUCCESS snapshot is not in COMPLETED state"
                );
            }
            return new RoutingExecutionResult(
                    snapshotMapper.restoreResponse(snapshot.storedResponse()),
                    snapshotMapper.restoreContext(originalRequest, snapshot),
                    RoutingDecision.SUCCESS,
                    null
            );
        }
        if (snapshot.decision() == TaskWorkflowExecutionDecision.FAIL) {
            if (snapshot.executionState()
                    != TaskWorkflowExecutionState.FAILED
                    || snapshot.storedFailure() == null) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "FAIL snapshot has no stored typed failure"
                );
            }
            throw new RoutingFailureException(
                    snapshotMapper.restoreFailure(snapshot.storedFailure()),
                    null
            );
        }
        if (snapshot.decision()
                == TaskWorkflowExecutionDecision.RETRY_LATER) {
            if (snapshot.executionState()
                    != TaskWorkflowExecutionState.RETRY_PENDING) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "RETRY_LATER snapshot is not in RETRY_PENDING state"
                );
            }
            return null;
        }
        throw new TaskWorkflowExecutionAlreadyInProgressException(
                "process:" + snapshot.processId()
        );
    }

    private void validateSnapshot(
            TaskWorkflowExecutionSnapshot snapshot,
            String executionId,
            RoutingPlan plan,
            String gatewayServiceVersion
    ) {
        if (snapshot.schemaVersion()
                != TaskWorkflowExecutionSnapshot.CURRENT_SCHEMA_VERSION) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Unsupported TASK_WORKFLOW snapshot schemaVersion="
                            + snapshot.schemaVersion()
            );
        }
        if (!executionId.equals(snapshot.executionId())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW executionId does not match the persisted snapshot"
            );
        }
        if (!gatewayServiceVersion.equals(
                snapshot.gatewayServiceVersion())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW retry gateway service version does not "
                            + "match the persisted request contract"
            );
        }
        RoutingPlanIdentity identity = plan.identity();
        boolean samePlan = identity.serviceCode().equals(snapshot.serviceCode())
                && identity.inboundAction().equalsIgnoreCase(
                snapshot.inboundAction())
                && identity.actionPlanName().equals(snapshot.actionPlanName())
                && identity.definitionId().equals(snapshot.definitionId())
                && identity.planFingerprint().equals(
                snapshot.planFingerprint())
                && plan.routingStrategy().name().equals(
                snapshot.routingStrategy());
        if (!samePlan) {
            throw new TaskWorkflowPlanChangedException(
                    "TASK_WORKFLOW plan changed during retry; persisted="
                            + snapshot.serviceCode() + ":"
                            + snapshot.inboundAction() + ":"
                            + snapshot.actionPlanName() + ":"
                            + snapshot.definitionId() + ":"
                            + snapshot.planFingerprint()
                            + ", current=" + plan.identity()
            );
        }
        validateSnapshotSteps(snapshot, plan);
    }

    private void validateSnapshotSteps(
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan
    ) {
        if (snapshot.steps().size() != plan.steps().size()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW snapshot step count does not match the "
                            + "current plan"
            );
        }
        for (int index = 0; index < plan.steps().size(); index++) {
            var expected = plan.steps().get(index);
            var stored = snapshot.steps().get(index);
            boolean matches = stored != null
                    && stored.stepIndex() == expected.stepIndex()
                    && expected.stepId().equals(stored.stepId())
                    && expected.observationContext().taskWorkflowStepType()
                    == stored.stepType()
                    && expected.serviceOperation().getOperationName()
                    .equals(stored.operationName());
            if (!matches) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "TASK_WORKFLOW snapshot step identity does not "
                                + "match the current plan at stepIndex="
                                + index
                );
            }
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
                                    + " has no workflow process"
                    );
                }
                if (suppliedProcessId != null
                        && suppliedProcessId.longValue()
                        != taskProcess.getAsLong()) {
                    throw new InvalidTaskWorkflowExecutionStateException(
                            "Conflicting TASK_WORKFLOW processId values from "
                                    + "the request and taskId=" + taskId
                    );
                }
                yield taskProcess.getAsLong();
            }
            default -> null;
        };
    }

    private TaskWorkflowRecoveryStore requiredStore(String serviceCode) {
        List<TaskWorkflowRecoveryStore> stores =
                storeProvider.orderedStream().toList();
        if (stores.size() != 1) {
            throw new IllegalStateException(
                    "Active TASK_WORKFLOW serviceCode=" + serviceCode
                            + " requires exactly one durable "
                            + "TaskWorkflowRecoveryStore; found "
                            + stores.size()
            );
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
                        + gatewayServiceVersion + ": " + reason
        );
    }

    private TaskWorkflowSnapshotUnavailableException snapshotUnavailable(
            String executionId,
            Long processId,
            TaskWorkflowCommandPlan plan,
            String reason
    ) {
        return new TaskWorkflowSnapshotUnavailableException(
                executionId,
                processId,
                plan.routingPlan().identity().serviceCode(),
                plan.inboundAction(),
                reason
        );
    }

    private String requireGatewayServiceVersion(Exchange exchange) {
        String gatewayServiceVersion = exchange.getProperty(
                Message.SERVICE_VERSION,
                String.class
        );
        if (gatewayServiceVersion == null
                || gatewayServiceVersion.isBlank()) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW gateway service version is unavailable"
            );
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
        if (failure instanceof TaskWorkflowAttemptConflictException) {
            return new TaskWorkflowExecutionAlreadyInProgressException(
                    "provider-attempt"
            );
        }
        String capacityDetail =
                failure instanceof TaskWorkflowSnapshotTooLargeException
                        ? ": " + failure.getMessage()
                        : "";
        return new TaskWorkflowPersistenceException(
                "Failed to " + action
                        + " TASK_WORKFLOW durable execution state"
                        + capacityDetail,
                failure
        );
    }

    private enum ExistingExecutionState {
        NEW_EXECUTION,
        EXISTING_SNAPSHOT,
        RECONSTRUCTABLE_START
    }

    private record ExistingExecution(
            ExistingExecutionState state,
            Long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        private ExistingExecution {
            if (state == ExistingExecutionState.EXISTING_SNAPSHOT
                    && snapshot == null) {
                throw new IllegalArgumentException(
                        "EXISTING_SNAPSHOT requires a snapshot"
                );
            }
            if (state != ExistingExecutionState.EXISTING_SNAPSHOT
                    && snapshot != null) {
                throw new IllegalArgumentException(
                        state + " must not contain a snapshot"
                );
            }
            if (state == ExistingExecutionState.RECONSTRUCTABLE_START
                    && processId == null) {
                throw new IllegalArgumentException(
                        "RECONSTRUCTABLE_START requires processId"
                );
            }
        }

        private static ExistingExecution newExecution(Long processId) {
            return new ExistingExecution(
                    ExistingExecutionState.NEW_EXECUTION,
                    processId,
                    null
            );
        }

        private static ExistingExecution existingSnapshot(
                Long processId,
                TaskWorkflowExecutionSnapshot snapshot
        ) {
            return new ExistingExecution(
                    ExistingExecutionState.EXISTING_SNAPSHOT,
                    processId,
                    snapshot
            );
        }

        private static ExistingExecution reconstructableStart(
                long processId
        ) {
            return new ExistingExecution(
                    ExistingExecutionState.RECONSTRUCTABLE_START,
                    processId,
                    null
            );
        }
    }

}
