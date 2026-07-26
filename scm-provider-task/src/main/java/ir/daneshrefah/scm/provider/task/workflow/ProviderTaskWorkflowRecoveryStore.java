package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.core.integration.service.routing.RoutingDecision;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.InvalidTaskWorkflowExecutionStateException;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.RoutingExecutionSnapshot;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.RoutingExecutionState;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowAttemptConflictException;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowPersistenceException;
import ir.daneshrefah.scm.core.integration.service.routing.taskworkflow.TaskWorkflowRecoveryStore;
import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceRepository;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceWatcherRepository;
import ir.daneshrefah.scm.provider.task.repository.TaskRepository;
import ir.daneshrefah.scm.provider.task.service.ProcessInstanceWatcherService;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;
import java.util.Set;

public class ProviderTaskWorkflowRecoveryStore
        implements TaskWorkflowRecoveryStore {
    private static final int WORKFLOW_EXECUTION_ROW = 0;
    private static final String TRANSACTION_MANAGER =
            "scmTaskProviderTransactionManager";

    private final ProcessInstanceRepository processRepository;
    private final ProcessInstanceWatcherRepository watcherRepository;
    private final TaskRepository taskRepository;
    private final ProcessInstanceWatcherService watcherService;
    private final ObjectMapper objectMapper;

    public ProviderTaskWorkflowRecoveryStore(
            ProcessInstanceRepository processRepository,
            ProcessInstanceWatcherRepository watcherRepository,
            TaskRepository taskRepository,
            ProcessInstanceWatcherService watcherService,
            ObjectMapper objectMapper
    ) {
        this.processRepository = processRepository;
        this.watcherRepository = watcherRepository;
        this.taskRepository = taskRepository;
        this.watcherService = watcherService;
        this.objectMapper = objectMapper;
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public OptionalLong findProcessIdByExecutionId(String executionId) {
        Set<Long> matches = new LinkedHashSet<>();
        processRepository.findByCorrelationId(executionId)
                .map(ProcessInstanceEntity::getId)
                .ifPresent(matches::add);
        watcherRepository.findAllByTypeAndRowNo(
                        ProcessWatcherEnum.WORKFLOW_EXECUTION,
                        WORKFLOW_EXECUTION_ROW
                )
                .forEach(watcher -> {
                    RoutingExecutionSnapshot snapshot = read(watcher);
                    validateSnapshotProcess(
                            watcher.getProcessInstance(),
                            snapshot
                    );
                    if (executionId.equals(snapshot.executionId())) {
                        matches.add(watcher.getProcessInstance().getId());
                    }
                });
        if (matches.size() > 1) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "executionId=" + executionId
                            + " resolves to multiple workflow processes");
        }
        return matches.isEmpty()
                ? OptionalLong.empty()
                : OptionalLong.of(matches.iterator().next());
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public OptionalLong findProcessIdByTaskId(long taskId) {
        return taskRepository.findById(taskId)
                .map(task -> task.getProcessInstance().getId())
                .map(OptionalLong::of)
                .orElseGet(OptionalLong::empty);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public Optional<RoutingExecutionSnapshot> loadForUpdate(
            long processId,
            String inboundAction,
            String gatewayServiceVersion
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        return watcher(processId)
                .map(this::read)
                .map(snapshot -> {
                    validateProcessState(process, snapshot);
                    validateRequestedIdentity(
                            snapshot,
                            inboundAction,
                            gatewayServiceVersion
                    );
                    return snapshot;
                });
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public RoutingExecutionSnapshot registerAttempt(
            long processId,
            RoutingExecutionSnapshot snapshot,
            String expectedAttemptId
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        Optional<ProcessInstanceWatcherEntity> existing = watcher(processId);
        validateSnapshotProcess(process, snapshot);
        if (existing.isPresent()) {
            RoutingExecutionSnapshot current = read(existing.get());
            validateProcessState(process, current);
            validateAttemptRegistration(current, snapshot, expectedAttemptId);
            write(existing.get(), snapshot);
        } else {
            if (expectedAttemptId != null) {
                throw attemptConflict(snapshot,
                        "expected attempt marker is unavailable");
            }
            watcherRepository.save(newWatcher(process, snapshot));
        }
        return snapshot;
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveProgress(
            long processId,
            RoutingExecutionSnapshot snapshot
    ) {
        persistExisting(processId, snapshot);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveRetryLater(
            long processId,
            RoutingExecutionSnapshot snapshot
    ) {
        if (snapshot.decision() != RoutingDecision.RETRY_LATER
                || snapshot.executionState()
                != RoutingExecutionState.RETRY_PENDING) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "saveRetryLater requires a RETRY_PENDING/RETRY_LATER snapshot");
        }
        persistExisting(processId, snapshot);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveCompleted(
            long processId,
            RoutingExecutionSnapshot snapshot
    ) {
        if (snapshot.decision() != RoutingDecision.SUCCESS
                || snapshot.executionState()
                != RoutingExecutionState.COMPLETED
                || snapshot.storedResponse() == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "saveCompleted requires a COMPLETED/SUCCESS snapshot with a response");
        }
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        Optional<ProcessInstanceWatcherEntity> existing = watcher(processId);
        if (existing.isEmpty()) {
            watcherRepository.save(newWatcher(process, snapshot));
            return;
        }
        validateOutcomeSave(read(existing.get()), snapshot);
        write(existing.get(), snapshot);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveFailed(
            long processId,
            RoutingExecutionSnapshot snapshot
    ) {
        if (snapshot.decision() != RoutingDecision.FAIL
                || snapshot.executionState() != RoutingExecutionState.FAILED
                || snapshot.storedFailure() == null) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "saveFailed requires a FAILED/FAIL snapshot with typed failure metadata");
        }
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        Optional<ProcessInstanceWatcherEntity> existing = watcher(processId);
        if (existing.isEmpty()) {
            watcherRepository.save(newWatcher(process, snapshot));
            return;
        }
        validateOutcomeSave(read(existing.get()), snapshot);
        write(existing.get(), snapshot);
    }

    private void persistExisting(
            long processId,
            RoutingExecutionSnapshot snapshot
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        ProcessInstanceWatcherEntity existing = watcher(processId)
                .orElseThrow(() -> new TaskWorkflowPersistenceException(
                        "Workflow execution watcher is unavailable for processId="
                                + processId));
        validateOutcomeSave(read(existing), snapshot);
        write(existing, snapshot);
    }

    private void validateAttemptRegistration(
            RoutingExecutionSnapshot current,
            RoutingExecutionSnapshot candidate,
            String expectedAttemptId
    ) {
        if (sameExecution(current, candidate)) {
            if (!Objects.equals(current.activeAttemptId(), expectedAttemptId)) {
                throw attemptConflict(candidate,
                        "attempt marker changed before the remote operation");
            }
            if (current.executionState() == RoutingExecutionState.FAILED
                    || current.executionState()
                    == RoutingExecutionState.COMPLETED) {
                throw attemptConflict(candidate,
                        "terminal execution cannot register another attempt");
            }
            return;
        }
        if (expectedAttemptId != null
                || current.executionState() == RoutingExecutionState.RUNNING
                || current.executionState()
                == RoutingExecutionState.RETRY_PENDING) {
            throw attemptConflict(candidate,
                    "another non-terminal workflow execution owns the process");
        }
    }

    private void validateOutcomeSave(
            RoutingExecutionSnapshot current,
            RoutingExecutionSnapshot candidate
    ) {
        if (!sameExecution(current, candidate)) {
            throw attemptConflict(candidate,
                    "workflow execution identity changed before outcome persistence");
        }
        if (!Objects.equals(
                current.activeAttemptId(),
                candidate.activeAttemptId())) {
            throw attemptConflict(candidate,
                    "attempt marker changed before outcome persistence");
        }
    }

    private boolean sameExecution(
            RoutingExecutionSnapshot first,
            RoutingExecutionSnapshot second
    ) {
        return first.executionId().equals(second.executionId())
                && first.planIdentity().equals(second.planIdentity())
                && first.gatewayServiceVersion().equals(
                second.gatewayServiceVersion())
                && Objects.equals(first.processId(), second.processId());
    }

    private void validateRequestedIdentity(
            RoutingExecutionSnapshot snapshot,
            String inboundAction,
            String gatewayServiceVersion
    ) {
        if (inboundAction == null
                || !snapshot.inboundAction().equalsIgnoreCase(inboundAction)
                || gatewayServiceVersion == null
                || !snapshot.gatewayServiceVersion().equals(
                gatewayServiceVersion)) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Workflow snapshot action or gateway service version "
                            + "does not match the retry request");
        }
    }

    private void validateSnapshotProcess(
            ProcessInstanceEntity process,
            RoutingExecutionSnapshot snapshot
    ) {
        if (process == null
                || snapshot.processId() == null
                || !snapshot.processId().equals(process.getId())) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Workflow snapshot processId does not match the locked process");
        }
    }

    private void validateProcessState(
            ProcessInstanceEntity process,
            RoutingExecutionSnapshot snapshot
    ) {
        validateSnapshotProcess(process, snapshot);
        if (snapshot.decision() != RoutingDecision.RETRY_LATER) {
            return;
        }
        boolean approvalSucceeded = snapshot.steps().stream()
                .anyMatch(step -> step.stepType()
                        == TaskWorkflowStepType.APPROVE_PROCESS
                        && step.decision() == RoutingDecision.SUCCESS);
        if (!approvalSucceeded) {
            return;
        }
        boolean waitingTask = process.getTasks() != null
                && process.getTasks().stream()
                .anyMatch(task -> task.getTaskStatus()
                        == TaskStatusEnum.WAITING_FOR_ACKNOWLEDGE);
        if (process.getProcessStatus()
                != ProcessStatusEnum.WAITING_FOR_ACKNOWLEDGE
                || !waitingTask) {
            throw new InvalidTaskWorkflowExecutionStateException(
                    "Workflow snapshot approval state conflicts with process/task "
                            + "state for processId=" + process.getId());
        }
    }

    private ProcessInstanceEntity lockProcess(long processId) {
        return processRepository.findByIdForWorkflowMutation(processId)
                .orElseThrow(() -> new TaskWorkflowPersistenceException(
                        "Workflow process is unavailable processId=" + processId));
    }

    private Optional<ProcessInstanceWatcherEntity> watcher(long processId) {
        return watcherRepository.findForWorkflowMutation(
                processId,
                ProcessWatcherEnum.WORKFLOW_EXECUTION,
                WORKFLOW_EXECUTION_ROW
        );
    }

    private ProcessInstanceWatcherEntity newWatcher(
            ProcessInstanceEntity process,
            RoutingExecutionSnapshot snapshot
    ) {
        return watcherService.createProcessInstanceWatcherEntity(
                        process,
                        objectMapper.valueToTree(snapshot),
                        ProcessWatcherEnum.WORKFLOW_EXECUTION
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new TaskWorkflowPersistenceException(
                        "Workflow watcher factory returned no row"));
    }

    private void write(
            ProcessInstanceWatcherEntity watcher,
            RoutingExecutionSnapshot snapshot
    ) {
        watcher.setData(objectMapper.valueToTree(snapshot));
        watcherRepository.save(watcher);
    }

    private RoutingExecutionSnapshot read(
            ProcessInstanceWatcherEntity watcher
    ) {
        try {
            return objectMapper.treeToValue(
                    watcher.getData(),
                    RoutingExecutionSnapshot.class
            );
        } catch (Exception exception) {
            throw new TaskWorkflowPersistenceException(
                    "Workflow execution snapshot JSON is invalid for watcherId="
                            + watcher.getId(),
                    exception
            );
        }
    }

    private TaskWorkflowAttemptConflictException attemptConflict(
            RoutingExecutionSnapshot snapshot,
            String reason
    ) {
        return new TaskWorkflowAttemptConflictException(
                "Concurrent TASK_WORKFLOW attempt rejected executionId="
                        + snapshot.executionId() + ": " + reason);
    }
}
