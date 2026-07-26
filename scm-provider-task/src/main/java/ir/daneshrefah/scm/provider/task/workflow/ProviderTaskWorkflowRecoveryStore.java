package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.taskworkflow.TaskWorkflowStepType;
import ir.daneshrefah.scm.provider.task.constant.ProcessStatusEnum;
import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.constant.TaskStatusEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceRepository;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceWatcherRepository;
import ir.daneshrefah.scm.provider.task.repository.TaskRepository;
import ir.daneshrefah.scm.provider.task.service.ProcessInstanceWatcherService;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Persists workflow recovery state in the existing process-watcher row.
 *
 * <p>The DATA mapping does not declare an expanded length, so the standard
 * JPA column capacity (255 characters) is enforced before each write. This
 * guard prevents database-specific truncation.</p>
 */
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
    public OptionalLong findProcessIdByCorrelationId(String correlationId) {
        Optional<ProcessInstanceEntity> process =
                processRepository.findByCorrelationId(correlationId);
        if (process.isEmpty()) {
            return OptionalLong.empty();
        }
        ProcessInstanceEntity existing = process.get();
        if (!Objects.equals(correlationId, existing.getCorrelationId())) {
            throw new TaskWorkflowRecoveryException(
                    "Correlated workflow process does not have an exact "
                            + "correlation match processId=" + existing.getId()
            );
        }
        return OptionalLong.of(existing.getId());
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
    public Optional<TaskWorkflowExecutionSnapshot> loadForUpdate(
            long processId
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        return watcher(processId)
                .map(this::read)
                .map(snapshot -> {
                    validateProcessState(process, snapshot);
                    return snapshot;
                });
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public TaskWorkflowExecutionSnapshot registerAttempt(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot,
            String expectedAttemptId
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        Optional<ProcessInstanceWatcherEntity> existing = watcher(processId);
        validateSnapshotProcess(process, snapshot);
        if (existing.isPresent()) {
            TaskWorkflowExecutionSnapshot current = read(existing.get());
            validateProcessState(process, current);
            validateAttemptRegistration(current, snapshot, expectedAttemptId);
            write(existing.get(), snapshot);
        } else {
            if (expectedAttemptId != null) {
                throw attemptConflict(
                        snapshot,
                        "expected attempt marker is unavailable"
                );
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
            TaskWorkflowExecutionSnapshot snapshot
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
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (snapshot.decision()
                != TaskWorkflowExecutionDecision.RETRY_LATER
                || snapshot.executionState()
                != TaskWorkflowExecutionState.RETRY_PENDING) {
            throw invalid(
                    "saveRetryLater requires a RETRY_PENDING/RETRY_LATER snapshot"
            );
        }
        persistOutcome(processId, snapshot);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveCompleted(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (snapshot.decision() != TaskWorkflowExecutionDecision.SUCCESS
                || snapshot.executionState()
                != TaskWorkflowExecutionState.COMPLETED
                || snapshot.storedResponse() == null) {
            throw invalid(
                    "saveCompleted requires a COMPLETED/SUCCESS snapshot "
                            + "with a response"
            );
        }
        persistTerminal(processId, snapshot);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW
    )
    public void saveFailed(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (snapshot.decision() != TaskWorkflowExecutionDecision.FAIL
                || snapshot.executionState()
                != TaskWorkflowExecutionState.FAILED
                || snapshot.storedFailure() == null) {
            throw invalid(
                    "saveFailed requires a FAILED/FAIL snapshot with typed "
                            + "failure metadata"
            );
        }
        persistTerminal(processId, snapshot);
    }

    private void persistTerminal(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        Optional<ProcessInstanceWatcherEntity> existing = watcher(processId);
        if (existing.isEmpty()) {
            watcherRepository.save(
                    newWatcher(process, snapshot.withoutActiveAttempt())
            );
            return;
        }
        validateOutcomeSave(read(existing.get()), snapshot);
        write(existing.get(), snapshot.withoutActiveAttempt());
    }

    private void persistExisting(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        ProcessInstanceWatcherEntity existing = watcher(processId)
                .orElseThrow(() -> new TaskWorkflowRecoveryException(
                        "Workflow execution watcher is unavailable for processId="
                                + processId
                ));
        validateOutcomeSave(read(existing), snapshot);
        write(existing, snapshot);
    }

    private void persistOutcome(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        Optional<ProcessInstanceWatcherEntity> existing = watcher(processId);
        if (existing.isEmpty()) {
            throw new TaskWorkflowRecoveryException(
                    "Workflow execution watcher is unavailable for processId="
                            + processId
            );
        }
        validateOutcomeSave(read(existing.get()), snapshot);
        write(existing.get(), snapshot.withoutActiveAttempt());
    }

    private void validateAttemptRegistration(
            TaskWorkflowExecutionSnapshot current,
            TaskWorkflowExecutionSnapshot candidate,
            String expectedAttemptId
    ) {
        if (sameExecution(current, candidate)) {
            if (!Objects.equals(
                    current.activeAttemptId(),
                    expectedAttemptId
            )) {
                throw attemptConflict(
                        candidate,
                        "attempt marker changed before the remote operation"
                );
            }
            if (current.executionState() == TaskWorkflowExecutionState.FAILED
                    || current.executionState()
                    == TaskWorkflowExecutionState.COMPLETED) {
                throw attemptConflict(
                        candidate,
                        "terminal execution cannot register another attempt"
                );
            }
            return;
        }
        if (expectedAttemptId != null
                || current.executionState()
                == TaskWorkflowExecutionState.RUNNING
                || current.executionState()
                == TaskWorkflowExecutionState.RETRY_PENDING) {
            throw attemptConflict(
                    candidate,
                    "another non-terminal workflow execution owns the process"
            );
        }
    }

    private void validateOutcomeSave(
            TaskWorkflowExecutionSnapshot current,
            TaskWorkflowExecutionSnapshot candidate
    ) {
        if (!sameExecution(current, candidate)) {
            throw attemptConflict(
                    candidate,
                    "workflow execution identity changed before outcome persistence"
            );
        }
        if (!Objects.equals(
                current.activeAttemptId(),
                candidate.activeAttemptId()
        )) {
            throw attemptConflict(
                    candidate,
                    "attempt marker changed before outcome persistence"
            );
        }
    }

    private boolean sameExecution(
            TaskWorkflowExecutionSnapshot first,
            TaskWorkflowExecutionSnapshot second
    ) {
        return first.executionId().equals(second.executionId())
                && first.serviceCode().equals(second.serviceCode())
                && first.inboundAction().equals(second.inboundAction())
                && first.actionPlanName().equals(second.actionPlanName())
                && first.definitionId().equals(second.definitionId())
                && first.planFingerprint().equals(second.planFingerprint())
                && first.gatewayServiceVersion().equals(
                second.gatewayServiceVersion())
                && Objects.equals(first.processId(), second.processId());
    }

    private void validateSnapshotProcess(
            ProcessInstanceEntity process,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (process == null
                || snapshot.processId() == null
                || !snapshot.processId().equals(process.getId())) {
            throw invalid(
                    "Workflow snapshot processId does not match the locked process"
            );
        }
    }

    private void validateProcessState(
            ProcessInstanceEntity process,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        validateSnapshotProcess(process, snapshot);
        if (snapshot.decision()
                != TaskWorkflowExecutionDecision.RETRY_LATER) {
            return;
        }
        boolean approvalSucceeded = snapshot.steps().stream()
                .anyMatch(step -> step.stepType()
                        == TaskWorkflowStepType.APPROVE_PROCESS
                        && step.decision()
                        == TaskWorkflowExecutionDecision.SUCCESS);
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
            throw invalid(
                    "Workflow snapshot approval state conflicts with process/task "
                            + "state for processId=" + process.getId()
            );
        }
    }

    private ProcessInstanceEntity lockProcess(long processId) {
        return processRepository.findByIdForWorkflowMutation(processId)
                .orElseThrow(() -> new TaskWorkflowRecoveryException(
                        "Workflow process is unavailable processId=" + processId
                ));
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
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        JsonNode serialized = serialize(snapshot);
        return watcherService.createProcessInstanceWatcherEntity(
                        process,
                        serialized,
                        ProcessWatcherEnum.WORKFLOW_EXECUTION
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> new TaskWorkflowRecoveryException(
                        "Workflow watcher factory returned no row"
                ));
    }

    private void write(
            ProcessInstanceWatcherEntity watcher,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        watcher.setData(serialize(snapshot));
        watcherRepository.save(watcher);
    }

    private JsonNode serialize(TaskWorkflowExecutionSnapshot snapshot) {
        try {
            JsonNode value = objectMapper.valueToTree(snapshot);
            String serialized = objectMapper.writeValueAsString(value);
            return objectMapper.readTree(serialized);
        } catch (TaskWorkflowRecoveryException exception) {
            throw exception;
        } catch (IllegalArgumentException | JsonProcessingException exception) {
            throw new TaskWorkflowRecoveryException(
                    "Workflow execution snapshot could not be serialized "
                            + "executionId=" + snapshot.executionId()
                            + ", processId=" + snapshot.processId(),
                    exception
            );
        }
    }

    private TaskWorkflowExecutionSnapshot read(
            ProcessInstanceWatcherEntity watcher
    ) {
        try {
            return objectMapper.treeToValue(
                    watcher.getData(),
                    TaskWorkflowExecutionSnapshot.class
            );
        } catch (Exception exception) {
            throw new TaskWorkflowRecoveryException(
                    "Workflow execution snapshot JSON is invalid for watcherId="
                            + watcher.getId(),
                    exception
            );
        }
    }

    private TaskWorkflowAttemptConflictException attemptConflict(
            TaskWorkflowExecutionSnapshot snapshot,
            String reason
    ) {
        return new TaskWorkflowAttemptConflictException(
                "Concurrent TASK_WORKFLOW attempt rejected executionId="
                        + snapshot.executionId() + ": " + reason
        );
    }

    private TaskWorkflowRecoveryException invalid(String message) {
        return new TaskWorkflowRecoveryException(message);
    }
}
