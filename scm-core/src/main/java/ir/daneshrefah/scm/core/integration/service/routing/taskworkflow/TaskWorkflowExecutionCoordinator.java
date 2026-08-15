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
import org.apache.camel.Exchange;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.UUID;

import static ir.daneshrefah.scm.provider.task.constant.ExecutionMethodTypeEnum.COMPLETE_TASK;

/**
 * Selects durable workflow identity, coordinates distributed aggregate locks,
 * and delegates ordered execution to routing engines.
 */
public class TaskWorkflowExecutionCoordinator {

    private final ObjectProvider<TaskWorkflowRecoveryStore> storeProvider;
    private final RoutingEngineRegistry engineRegistry;
    private final RoutingRecoveryPolicyRegistry recoveryPolicyRegistry;
    private final TaskWorkflowExecutionIdentityResolver identityResolver;
    private final TaskWorkflowInputResolver inputResolver;
    private final TaskWorkflowPayloadMapper payloadMapper;
    private final TaskWorkflowSnapshotMapper snapshotMapper;
    private final TaskWorkflowTransactionCoordinator transactionCoordinator;
    private final TaskWorkflowDistributedLock distributedLock;

    public TaskWorkflowExecutionCoordinator(
            ObjectProvider<TaskWorkflowRecoveryStore> storeProvider,
            RoutingEngineRegistry engineRegistry,
            RoutingRecoveryPolicyRegistry recoveryPolicyRegistry,
            TaskWorkflowExecutionIdentityResolver identityResolver,
            TaskWorkflowInputResolver inputResolver,
            TaskWorkflowPayloadMapper payloadMapper,
            TaskWorkflowSnapshotMapper snapshotMapper,
            TaskWorkflowTransactionCoordinator transactionCoordinator,
            TaskWorkflowDistributedLock distributedLock
    ) {
        this.storeProvider = storeProvider;
        this.engineRegistry = engineRegistry;
        this.recoveryPolicyRegistry = recoveryPolicyRegistry;
        this.identityResolver = identityResolver;
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
        TaskWorkflowExecutionIdentityResolver.ResolvedRequestIdentity request =
                identityResolver.resolve(exchange);
        if (request.scmClientCorrelationId() != null) {
            exchange.setProperty(
                    Message.CLIENT_CORRELATION_ID,
                    request.scmClientCorrelationId()
            );
        }

        if (commandPlan.command() == TaskWorkflowCommand.START_PROCESS) {
            return executeStart(
                    exchange,
                    commandPlan,
                    request,
                    gatewayServiceVersion,
                    store
            );
        }

        Long processId = initialProcessId(exchange, plan, store);
        if (processId != null) {
            long selectedProcessId = processId;
            return distributedLock.withProcessLock(
                    serviceCode,
                    selectedProcessId,
                    () -> {
                        Long rechecked = initialProcessId(
                                exchange,
                                plan,
                                store
                        );
                        if (!Objects.equals(selectedProcessId, rechecked)) {
                            throw snapshotUnavailable(
                                    request.suppliedExecutionId(),
                                    selectedProcessId,
                                    commandPlan,
                                    "process identity changed while acquiring "
                                            + "the process lock"
                            );
                        }
                        return executeProcessCommand(
                                exchange,
                                commandPlan,
                                request,
                                selectedProcessId,
                                gatewayServiceVersion,
                                store
                        );
                    }
            );
        }

        if (request.suppliedExecutionId() != null) {
            throw unavailable(
                    request.suppliedExecutionId(),
                    commandPlan,
                    gatewayServiceVersion,
                    "executionId cannot select an execution without processId "
                            + "or a taskId that resolves to a process"
            );
        }
        String invocationId = UUID.randomUUID().toString();
        return executeFresh(
                exchange,
                commandPlan,
                invocationId,
                null,
                null,
                gatewayServiceVersion,
                store
        );
    }

    private RoutingExecutionResult executeStart(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionIdentityResolver.ResolvedRequestIdentity request,
            String gatewayServiceVersion,
            TaskWorkflowRecoveryStore store
    ) {
        String clientCorrelation = request.scmClientCorrelationId();
        if (clientCorrelation == null) {
            rejectClientExecutionIdForNewStart(
                    request.suppliedExecutionId()
            );
            String executionId = UUID.randomUUID().toString();
            return executeFresh(
                    exchange,
                    commandPlan,
                    executionId,
                    null,
                    executionId,
                    gatewayServiceVersion,
                    store
            );
        }

        String serviceCode =
                commandPlan.routingPlan().identity().serviceCode();
        OptionalLong optimistic = findCorrelatedProcess(
                store,
                serviceCode,
                clientCorrelation
        );
        if (optimistic.isPresent()) {
            return executeExistingStartWithProcessLock(
                    exchange,
                    commandPlan,
                    request,
                    optimistic.getAsLong(),
                    gatewayServiceVersion,
                    store
            );
        }

        StartResolution resolution = distributedLock.withStartLock(
                serviceCode,
                clientCorrelation,
                () -> {
                    OptionalLong rechecked = findCorrelatedProcess(
                            store,
                            serviceCode,
                            clientCorrelation
                    );
                    if (rechecked.isPresent()) {
                        return new StartResolution.ExistingProcess(
                                rechecked.getAsLong()
                        );
                    }
                    rejectClientExecutionIdForNewStart(
                            request.suppliedExecutionId()
                    );
                    String executionId = UUID.randomUUID().toString();
                    return new StartResolution.Completed(executeFresh(
                            exchange,
                            commandPlan,
                            executionId,
                            null,
                            clientCorrelation,
                            gatewayServiceVersion,
                            store
                    ));
                }
        );
        if (resolution instanceof StartResolution.Completed completed) {
            return completed.result();
        }
        long processId = ((StartResolution.ExistingProcess) resolution)
                .processId();
        return executeExistingStartWithProcessLock(
                exchange,
                commandPlan,
                request,
                processId,
                gatewayServiceVersion,
                store
        );
    }

    private RoutingExecutionResult executeExistingStartWithProcessLock(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionIdentityResolver.ResolvedRequestIdentity request,
            long selectedProcessId,
            String gatewayServiceVersion,
            TaskWorkflowRecoveryStore store
    ) {
        String serviceCode =
                commandPlan.routingPlan().identity().serviceCode();
        String clientCorrelation = request.scmClientCorrelationId();
        return distributedLock.withProcessLock(
                serviceCode,
                selectedProcessId,
                () -> {
                    OptionalLong rechecked = findCorrelatedProcess(
                            store,
                            serviceCode,
                            clientCorrelation
                    );
                    if (rechecked.isEmpty()
                            || rechecked.getAsLong() != selectedProcessId) {
                        throw snapshotUnavailable(
                                request.suppliedExecutionId(),
                                selectedProcessId,
                                commandPlan,
                                "service-scoped client correlation changed "
                                        + "while acquiring the process lock"
                        );
                    }
                    String persistedCorrelation =
                            requireExactProcessCorrelation(
                                    store,
                                    selectedProcessId,
                                    clientCorrelation,
                                    commandPlan
                            );
                    Optional<TaskWorkflowExecutionSnapshot> loaded =
                            load(store, selectedProcessId);
                    if (loaded.isEmpty()) {
                        if (request.suppliedExecutionId() != null) {
                            throw snapshotUnavailable(
                                    request.suppliedExecutionId(),
                                    selectedProcessId,
                                    commandPlan,
                                    "workflow watcher is missing, so the "
                                            + "supplied executionId cannot be "
                                            + "validated"
                            );
                        }
                        requireReconstructableStart(
                                commandPlan,
                                selectedProcessId
                        );
                        String executionId = UUID.randomUUID().toString();
                        return executeFresh(
                                exchange,
                                commandPlan,
                                executionId,
                                selectedProcessId,
                                persistedCorrelation,
                                gatewayServiceVersion,
                                store
                        );
                    }
                    return executeExistingStart(
                            exchange,
                            commandPlan,
                            request.suppliedExecutionId(),
                            persistedCorrelation,
                            gatewayServiceVersion,
                            store,
                            loaded.get()
                    );
                }
        );
    }

    private RoutingExecutionResult executeExistingStart(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            String suppliedExecutionId,
            String processCorrelation,
            String gatewayServiceVersion,
            TaskWorkflowRecoveryStore store,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        validateSuppliedExecutionId(snapshot, suppliedExecutionId);
        validateSnapshotService(snapshot, commandPlan);
        if (!sameAction(snapshot, commandPlan.inboundAction())) {
            if (!isTerminal(snapshot)) {
                throw new TaskWorkflowExecutionAlreadyInProgressException(
                        "process:" + snapshot.execution().processId()
                );
            }
            throw snapshotUnavailable(
                    snapshot.execution().executionId(),
                    snapshot.execution().processId(),
                    commandPlan,
                    "the current workflow watcher belongs to a later inbound "
                            + "action, so the original START response cannot "
                            + "be replayed safely"
            );
        }
        validateSnapshot(
                snapshot,
                commandPlan.routingPlan(),
                gatewayServiceVersion
        );
        return executeExistingSnapshot(
                exchange,
                commandPlan,
                processCorrelation,
                store,
                snapshot
        );
    }

    private RoutingExecutionResult executeProcessCommand(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowExecutionIdentityResolver.ResolvedRequestIdentity request,
            long processId,
            String gatewayServiceVersion,
            TaskWorkflowRecoveryStore store
    ) {
        TaskWorkflowExecutionSnapshot snapshot = load(store, processId)
                .orElseThrow(() -> snapshotUnavailable(
                        request.suppliedExecutionId(),
                        processId,
                        commandPlan,
                        "workflow watcher is missing; process-backed commands "
                                + "must not restart or guess completed steps"
                ));
        validateSuppliedExecutionId(
                snapshot,
                request.suppliedExecutionId()
        );
        validateSnapshotService(snapshot, commandPlan);
        String processCorrelation = findProcessCorrelation(store, processId)
                .orElse(null);

        if (sameAction(snapshot, commandPlan.inboundAction()) && commandPlan.routingPlan().routingStrategy() != RoutingStrategy.FIRST) {
            validateSnapshot(
                    snapshot,
                    commandPlan.routingPlan(),
                    gatewayServiceVersion
            );
            return executeExistingSnapshot(
                    exchange,
                    commandPlan,
                    processCorrelation,
                    store,
                    snapshot
            );
        }
        if (!isTerminal(snapshot)) {
            throw new TaskWorkflowExecutionAlreadyInProgressException(
                    "process:" + processId
            );
        }
        return executeFresh(
                exchange,
                commandPlan,
                snapshot.execution().executionId(),
                processId,
                processCorrelation,
                gatewayServiceVersion,
                store
        );
    }

    private RoutingExecutionResult executeExistingSnapshot(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            String processCorrelation,
            TaskWorkflowRecoveryStore store,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        Object originalRequest = exchange.getMessage().getBody();
        applyExecutionProperties(
                exchange,
                snapshot.execution().executionId(),
                snapshot.execution().processId(),
                processCorrelation
        );
        RoutingExecutionResult terminal = terminalResult(
                originalRequest,
                snapshot,
                plan,
                processCorrelation
        );
        if (terminal != null) {
            return terminal;
        }
        RoutingExecutionContext context = snapshotMapper.restoreContext(
                originalRequest,
                snapshot,
                plan,
                processCorrelation
        );
        RoutingCursor cursor = recoveryPolicyRegistry
                .getRequired(plan.routingStrategy())
                .resolveRetryCursor(plan, snapshot);
        return run(
                exchange,
                commandPlan,
                store,
                snapshot,
                context,
                cursor
        );
    }

    private RoutingExecutionResult executeFresh(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            String executionId,
            Long processId,
            String processCorrelation,
            String gatewayServiceVersion,
            TaskWorkflowRecoveryStore store
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        Object originalRequest = exchange.getMessage().getBody();
        RoutingExecutionContext context =
                new RoutingExecutionContext(originalRequest);
        context.processId(processId);
        context.correlationId(processCorrelation);
        TaskWorkflowExecutionSnapshot snapshot = snapshotMapper.initial(
                executionId,
                plan,
                gatewayServiceVersion,
                processId,
                context,
                Instant.now()
        );
        applyExecutionProperties(
                exchange,
                executionId,
                processId,
                processCorrelation
        );
        return run(
                exchange,
                commandPlan,
                store,
                snapshot,
                context,
                RoutingCursor.start(plan)
        );
    }

    private RoutingExecutionResult run(
            Exchange exchange,
            TaskWorkflowCommandPlan commandPlan,
            TaskWorkflowRecoveryStore store,
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingExecutionContext context,
            RoutingCursor cursor
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        TaskWorkflowExecutionLifecycle lifecycle =
                new TaskWorkflowExecutionLifecycle(
                        store,
                        commandPlan,
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

    private void applyExecutionProperties(
            Exchange exchange,
            String executionId,
            Long processId,
            String processCorrelation
    ) {
        exchange.setProperty(Message.EXECUTION_ID, executionId);
        if (processId != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.PROCESS_ID,
                    processId
            );
        }
        if (processCorrelation != null) {
            exchange.setProperty(
                    TaskWorkflowExchangeProperties.CORRELATION_ID,
                    processCorrelation
            );
        }
    }

    private RoutingExecutionResult terminalResult(
            Object originalRequest,
            TaskWorkflowExecutionSnapshot snapshot,
            RoutingPlan plan,
            String processCorrelation
    ) {
        if (snapshot.status().decision()
                == TaskWorkflowExecutionDecision.SUCCESS) {
            if (snapshot.status().state()
                    != TaskWorkflowExecutionState.COMPLETED) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "SUCCESS snapshot is not in COMPLETED state"
                );
            }
            return new RoutingExecutionResult(
                    snapshotMapper.restoreResponse(snapshot, plan),
                    snapshotMapper.restoreContext(
                            originalRequest,
                            snapshot,
                            plan,
                            processCorrelation
                    ),
                    RoutingDecision.SUCCESS,
                    null
            );
        }
        if (snapshot.status().decision()
                == TaskWorkflowExecutionDecision.FAIL) {
            if (snapshot.status().state()
                    != TaskWorkflowExecutionState.FAILED
                    || snapshot.terminal() == null
                    || snapshot.terminal().failure() == null) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "FAIL snapshot has no stored typed failure"
                );
            }
            throw new RoutingFailureException(
                    snapshotMapper.restoreFailure(snapshot, plan),
                    null
            );
        }
        if (snapshot.status().decision()
                == TaskWorkflowExecutionDecision.RETRY_LATER) {
            if (snapshot.status().state()
                    != TaskWorkflowExecutionState.RETRY_PENDING) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "RETRY_LATER snapshot is not in RETRY_PENDING state"
                );
            }
            return null;
        }
        throw new TaskWorkflowExecutionAlreadyInProgressException(
                "process:" + snapshot.execution().processId()
        );
    }

    private void validateSnapshot(
            TaskWorkflowExecutionSnapshot snapshot,
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
        if (!gatewayServiceVersion.equals(
                snapshot.execution().gatewayServiceVersion())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW retry gateway service version does not "
                            + "match the persisted request contract"
            );
        }
        RoutingPlanIdentity identity = plan.identity();
        TaskWorkflowExecutionSnapshot.PlanIdentity stored = snapshot.plan();
        boolean samePlan = identity.serviceCode().equals(
                stored.serviceCode())
                && identity.inboundAction().equalsIgnoreCase(
                stored.inboundAction())
                && identity.actionPlanName().equals(stored.actionPlanName())
                && identity.definitionId().equals(stored.definitionId())
                && identity.planFingerprint().equals(stored.fingerprint());
        if (!samePlan) {
            throw new TaskWorkflowPlanChangedException(
                    "TASK_WORKFLOW plan changed during retry; persisted="
                            + stored.serviceCode() + ":"
                            + stored.inboundAction() + ":"
                            + stored.actionPlanName() + ":"
                            + stored.definitionId() + ":"
                            + stored.fingerprint()
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
            if (stored == null
                    || stored.stepIndex() != expected.stepIndex()
                    || !expected.stepId().equals(stored.stepId())) {
                throw new InvalidTaskWorkflowExecutionStateException(
                        "TASK_WORKFLOW snapshot step identity does not "
                                + "match the current plan at stepIndex="
                                + index
                );
            }
        }
    }

    private void validateSuppliedExecutionId(
            TaskWorkflowExecutionSnapshot snapshot,
            String suppliedExecutionId
    ) {
        if (suppliedExecutionId != null
                && !suppliedExecutionId.equals(
                snapshot.execution().executionId())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "TASK_WORKFLOW supplied executionId does not match the "
                            + "persisted server executionId"
            );
        }
    }

    private void validateSnapshotService(
            TaskWorkflowExecutionSnapshot snapshot,
            TaskWorkflowCommandPlan commandPlan
    ) {
        String requestedService =
                commandPlan.routingPlan().identity().serviceCode();
        if (!requestedService.equals(snapshot.plan().serviceCode())) {
            throw snapshotUnavailable(
                    snapshot.execution().executionId(),
                    snapshot.execution().processId(),
                    commandPlan,
                    "process workflow snapshot belongs to serviceCode="
                            + snapshot.plan().serviceCode()
            );
        }
    }

    private void rejectClientExecutionIdForNewStart(
            String suppliedExecutionId
    ) {
        if (suppliedExecutionId != null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "A new TASK_WORKFLOW START must not supply executionId; "
                            + "SCM generates the authoritative executionId"
            );
        }
    }

    private boolean sameAction(
            TaskWorkflowExecutionSnapshot snapshot,
            String inboundAction
    ) {
        return inboundAction.equalsIgnoreCase(
                snapshot.plan().inboundAction()
        );
    }

    private boolean isTerminal(TaskWorkflowExecutionSnapshot snapshot) {
        return snapshot.status().state()
                == TaskWorkflowExecutionState.COMPLETED
                && snapshot.status().decision()
                == TaskWorkflowExecutionDecision.SUCCESS
                || snapshot.status().state()
                == TaskWorkflowExecutionState.FAILED
                && snapshot.status().decision()
                == TaskWorkflowExecutionDecision.FAIL;
    }

    private void requireReconstructableStart(
            TaskWorkflowCommandPlan commandPlan,
            long processId
    ) {
        RoutingPlan plan = commandPlan.routingPlan();
        boolean reconstructable = commandPlan.command()
                == TaskWorkflowCommand.START_PROCESS
                && plan.routingStrategy() == RoutingStrategy.FIRST
                && plan.steps().size() == 1
                && plan.steps().getFirst().observationContext()
                .taskWorkflowStepType()
                == TaskWorkflowStepType.START_PROCESS;
        if (!reconstructable) {
            throw snapshotUnavailable(
                    null,
                    processId,
                    commandPlan,
                    "workflow watcher is missing and the action plan is not "
                            + "a one-step FIRST/START_PROCESS plan"
            );
        }
    }

    private String requireExactProcessCorrelation(
            TaskWorkflowRecoveryStore store,
            long processId,
            String expected,
            TaskWorkflowCommandPlan commandPlan
    ) {
        String actual = findProcessCorrelation(store, processId)
                .orElseThrow(() -> snapshotUnavailable(
                        null,
                        processId,
                        commandPlan,
                        "correlated START process has no persisted "
                                + "correlationId"
                ));
        if (!actual.equals(expected)) {
            throw snapshotUnavailable(
                    null,
                    processId,
                    commandPlan,
                    "persisted process correlationId does not exactly match "
                            + "scmClientCorrelationId"
            );
        }
        return actual;
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
            String serviceCode,
            String clientCorrelation
    ) {
        try {
            return store.findProcessIdByClientCorrelation(
                    serviceCode,
                    clientCorrelation
            );
        } catch (RuntimeException failure) {
            throw persistenceFailure(
                    "resolve the service-scoped correlated process",
                    failure
            );
        }
    }

    private Optional<String> findProcessCorrelation(
            TaskWorkflowRecoveryStore store,
            long processId
    ) {
        try {
            return store.findProcessCorrelationId(processId);
        } catch (RuntimeException failure) {
            throw persistenceFailure(
                    "resolve process correlation",
                    failure
            );
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
            case APPROVE_PROCESS, REJECT_PROCESS,
                 GET_TASK, UPDATE_DESCRIPTION ,
                 COMPLETE_PROCESS ->
                    inputResolver.resolveProcessId(exchange, stepType);
            case TASK_COMPLETE -> {
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
        return new TaskWorkflowPersistenceException(
                "Failed to " + action
                        + " TASK_WORKFLOW durable execution state",
                failure
        );
    }

    private sealed interface StartResolution {
        record Completed(RoutingExecutionResult result)
                implements StartResolution {
        }

        record ExistingProcess(long processId)
                implements StartResolution {
        }
    }
}
