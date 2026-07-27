package ir.daneshrefah.scm.provider.task.workflow;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.provider.task.constant.ProcessWatcherEnum;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceEntity;
import ir.daneshrefah.scm.provider.task.entity.ProcessInstanceWatcherEntity;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceRepository;
import ir.daneshrefah.scm.provider.task.repository.ProcessInstanceWatcherRepository;
import ir.daneshrefah.scm.provider.task.repository.TaskRepository;
import ir.daneshrefah.scm.provider.task.service.ProcessInstanceWatcherService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Persists versioned workflow recovery state in the existing current
 * process-watcher row. No storage size is inferred from the JPA default.
 */
public class ProviderTaskWorkflowRecoveryStore
        implements TaskWorkflowRecoveryStore {
    private static final int LEGACY_SCHEMA_VERSION = 1;
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
    public OptionalLong findProcessIdByClientCorrelation(
            String serviceCode,
            String scmClientCorrelationId
    ) {
        String requiredServiceCode = StringUtils.trimToNull(serviceCode);
        String requiredCorrelation = StringUtils.trimToNull(
                scmClientCorrelationId
        );
        if (requiredServiceCode == null || requiredCorrelation == null) {
            throw invalid(
                    "Service code and client correlation are required for "
                            + "correlated workflow lookup"
            );
        }
        Optional<ProcessInstanceEntity> process =
                processRepository.findByCorrelationId(requiredCorrelation);
        if (process.isEmpty()) {
            return OptionalLong.empty();
        }
        ProcessInstanceEntity existing = process.get();
        if (!Objects.equals(
                requiredCorrelation,
                existing.getCorrelationId()
        )) {
            throw invalid(
                    "Correlated workflow process does not have an exact "
                            + "correlation match processId=" + existing.getId()
            );
        }
        currentWatcher(existing.getId()).ifPresent(watcher -> {
            TaskWorkflowExecutionSnapshot snapshot = read(watcher);
            if (!requiredServiceCode.equalsIgnoreCase(
                    snapshot.plan().serviceCode()
            )) {
                throw invalid(
                        "Client correlation resolves to another workflow "
                                + "service processId=" + existing.getId()
                                + ", requestedServiceCode="
                                + requiredServiceCode
                );
            }
        });
        /*
         * A missing workflow watcher is intentionally returned to core. Core
         * permits it only for the tightly constrained, idempotent START
         * reconstruction path and revalidates the start request under the
         * process lock.
         */
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
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public Optional<String> findProcessCorrelationId(long processId) {
        return processRepository.findById(processId)
                .map(ProcessInstanceEntity::getCorrelationId)
                .map(StringUtils::trimToNull);
    }

    @Override
    @Transactional(
            transactionManager = TRANSACTION_MANAGER,
            propagation = Propagation.REQUIRES_NEW,
            readOnly = true
    )
    public Map<Long, String> findExecutionIdsByProcessIds(
            Collection<Long> processIds
    ) {
        if (processIds == null || processIds.isEmpty()) {
            return Map.of();
        }
        List<Long> ids = processIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        Map<Long, String> result = new LinkedHashMap<>();
        for (ProcessInstanceWatcherEntity watcher :
                watcherRepository.findCurrentByProcessIds(
                        ids,
                        ProcessWatcherEnum.WORKFLOW_EXECUTION,
                        WORKFLOW_EXECUTION_ROW
                )) {
            long processId = watcher.getProcessInstance().getId();
            String previous = result.putIfAbsent(
                    processId,
                    read(watcher).execution().executionId()
            );
            if (previous != null) {
                throw invalid(
                        "More than one current workflow watcher exists for "
                                + "processId=" + processId
                );
            }
        }
        return Map.copyOf(result);
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
                    validateSnapshotProcess(process, snapshot);
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
            validateSnapshotProcess(process, current);
            validateAttemptRegistration(
                    current,
                    snapshot,
                    expectedAttemptId
            );
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
        if (snapshot.status().decision()
                != TaskWorkflowExecutionDecision.RETRY_LATER
                || snapshot.status().state()
                != TaskWorkflowExecutionState.RETRY_PENDING) {
            throw invalid(
                    "saveRetryLater requires a RETRY_PENDING/RETRY_LATER "
                            + "snapshot"
            );
        }
        requireResponseOnly(snapshot, "saveRetryLater");
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
        if (snapshot.status().decision()
                != TaskWorkflowExecutionDecision.SUCCESS
                || snapshot.status().state()
                != TaskWorkflowExecutionState.COMPLETED) {
            throw invalid(
                    "saveCompleted requires a COMPLETED/SUCCESS snapshot"
            );
        }
        requireResponseOnly(snapshot, "saveCompleted");
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
        if (snapshot.status().decision()
                != TaskWorkflowExecutionDecision.FAIL
                || snapshot.status().state()
                != TaskWorkflowExecutionState.FAILED
                || snapshot.terminal() == null
                || snapshot.terminal().failure() == null
                || snapshot.terminal().response() != null) {
            throw invalid(
                    "saveFailed requires a FAILED/FAIL snapshot with typed "
                            + "failure metadata"
            );
        }
        persistTerminal(processId, snapshot);
    }

    private void requireResponseOnly(
            TaskWorkflowExecutionSnapshot snapshot,
            String operation
    ) {
        if (snapshot.terminal() == null
                || snapshot.terminal().response() == null
                || snapshot.terminal().failure() != null) {
            throw invalid(operation + " requires a stored response only");
        }
    }

    private void persistTerminal(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        ProcessInstanceWatcherEntity existing = watcher(processId)
                .orElseThrow(() -> invalid(
                        "Workflow execution watcher is unavailable for "
                                + "terminal persistence processId=" + processId
                ));
        validateOutcomeSave(read(existing), snapshot);
        write(existing, snapshot.withoutActiveAttempt());
    }

    private void persistExisting(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        ProcessInstanceEntity process = lockProcess(processId);
        validateSnapshotProcess(process, snapshot);
        ProcessInstanceWatcherEntity existing = watcher(processId)
                .orElseThrow(() -> invalid(
                        "Workflow execution watcher is unavailable for "
                                + "processId=" + processId
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
        ProcessInstanceWatcherEntity existing = watcher(processId)
                .orElseThrow(() -> invalid(
                        "Workflow execution watcher is unavailable for "
                                + "processId=" + processId
                ));
        validateOutcomeSave(read(existing), snapshot);
        write(existing, snapshot.withoutActiveAttempt());
    }

    private void validateAttemptRegistration(
            TaskWorkflowExecutionSnapshot current,
            TaskWorkflowExecutionSnapshot candidate,
            String expectedAttemptId
    ) {
        if (sameExecution(current, candidate)) {
            if (!Objects.equals(
                    current.status().activeAttemptId(),
                    expectedAttemptId
            )) {
                throw attemptConflict(
                        candidate,
                        "attempt marker changed before the remote operation"
                );
            }
            if (current.status().state()
                    == TaskWorkflowExecutionState.FAILED
                    || current.status().state()
                    == TaskWorkflowExecutionState.COMPLETED) {
                throw attemptConflict(
                        candidate,
                        "terminal execution cannot register another attempt"
                );
            }
            return;
        }
        if (expectedAttemptId != null
                || current.status().state()
                == TaskWorkflowExecutionState.RUNNING
                || current.status().state()
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
                    "workflow execution identity changed before outcome "
                            + "persistence"
            );
        }
        if (!Objects.equals(
                current.status().activeAttemptId(),
                candidate.status().activeAttemptId()
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
        return first.execution().executionId().equals(
                second.execution().executionId()
        )
                && first.plan().equals(second.plan())
                && first.execution().gatewayServiceVersion().equals(
                second.execution().gatewayServiceVersion())
                && Objects.equals(
                first.execution().processId(),
                second.execution().processId()
        );
    }

    private void validateSnapshotProcess(
            ProcessInstanceEntity process,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        if (process == null
                || snapshot.execution().processId() == null
                || !snapshot.execution().processId().equals(process.getId())) {
            throw invalid(
                    "Workflow snapshot processId does not match the locked "
                            + "process"
            );
        }
    }

    private ProcessInstanceEntity lockProcess(long processId) {
        return processRepository.findByIdForWorkflowMutation(processId)
                .orElseThrow(() -> invalid(
                        "Workflow process is unavailable processId="
                                + processId
                ));
    }

    private Optional<ProcessInstanceWatcherEntity> watcher(long processId) {
        return watcherRepository.findForWorkflowMutation(
                processId,
                ProcessWatcherEnum.WORKFLOW_EXECUTION,
                WORKFLOW_EXECUTION_ROW
        );
    }

    private Optional<ProcessInstanceWatcherEntity> currentWatcher(
            long processId
    ) {
        return watcherRepository.findCurrentByProcessIds(
                        List.of(processId),
                        ProcessWatcherEnum.WORKFLOW_EXECUTION,
                        WORKFLOW_EXECUTION_ROW
                )
                .stream()
                .findFirst();
    }

    private ProcessInstanceWatcherEntity newWatcher(
            ProcessInstanceEntity process,
            TaskWorkflowExecutionSnapshot snapshot
    ) {
        return watcherService.createProcessInstanceWatcherEntity(
                        process,
                        serialize(snapshot),
                        ProcessWatcherEnum.WORKFLOW_EXECUTION
                )
                .stream()
                .findFirst()
                .orElseThrow(() -> invalid(
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
        if (snapshot.schemaVersion()
                != TaskWorkflowExecutionSnapshot.CURRENT_SCHEMA_VERSION) {
            throw invalid(
                    "Only task-workflow snapshot schemaVersion="
                            + TaskWorkflowExecutionSnapshot
                            .CURRENT_SCHEMA_VERSION + " may be written"
            );
        }
        try {
            return objectMapper.valueToTree(snapshot);
        } catch (IllegalArgumentException exception) {
            throw new TaskWorkflowRecoveryException(
                    "Workflow execution snapshot could not be serialized "
                            + "executionId="
                            + snapshot.execution().executionId()
                            + ", processId="
                            + snapshot.execution().processId(),
                    exception
            );
        }
    }

    private TaskWorkflowExecutionSnapshot read(
            ProcessInstanceWatcherEntity watcher
    ) {
        try {
            JsonNode data = watcher.getData();
            if (data == null || !data.isObject()) {
                throw invalid("Workflow execution snapshot is not an object");
            }
            int schemaVersion = data.path("schemaVersion").asInt(-1);
            return switch (schemaVersion) {
                case LEGACY_SCHEMA_VERSION -> migrateLegacy(data);
                case TaskWorkflowExecutionSnapshot.CURRENT_SCHEMA_VERSION ->
                        objectMapper.treeToValue(
                                data,
                                TaskWorkflowExecutionSnapshot.class
                        );
                default -> throw invalid(
                        "Unsupported workflow execution snapshot "
                                + "schemaVersion=" + schemaVersion
                );
            };
        } catch (TaskWorkflowRecoveryException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new TaskWorkflowRecoveryException(
                    "Workflow execution snapshot JSON is invalid for "
                            + "watcherId=" + watcher.getId(),
                    exception
            );
        }
    }

    private TaskWorkflowExecutionSnapshot migrateLegacy(JsonNode legacy) {
        List<TaskWorkflowExecutionSnapshot.StepState> steps =
                new ArrayList<>();
        JsonNode legacySteps = legacy.path("steps");
        if (!legacySteps.isArray()) {
            throw invalid("Legacy workflow snapshot steps are invalid");
        }
        for (JsonNode step : legacySteps) {
            steps.add(new TaskWorkflowExecutionSnapshot.StepState(
                    requiredText(step, "stepId"),
                    requiredInt(step, "stepIndex"),
                    enumValue(
                            step.get("decision"),
                            TaskWorkflowExecutionDecision.class
                    ),
                    step.path("attemptCount").asInt(0),
                    nullableText(step.get("normalizedOutcome")),
                    nullableText(step.get("reasonCode")),
                    enumValue(step.get("messageStatus"), MessageStatus.class),
                    instant(step.get("attemptedAt"))
            ));
        }

        TaskWorkflowExecutionState state = enumValue(
                legacy.get("executionState"),
                TaskWorkflowExecutionState.class
        );
        TaskWorkflowExecutionDecision decision = enumValue(
                legacy.get("decision"),
                TaskWorkflowExecutionDecision.class
        );
        JsonNode legacyResume = legacy.get("resumeData");
        TaskWorkflowExecutionSnapshot.ResumeData resume =
                legacyResume == null || legacyResume.isNull()
                        ? null
                        : new TaskWorkflowExecutionSnapshot.ResumeData(
                                copy(legacyResume.get("transactionData")),
                                copy(legacyResume.get("retryRequest")),
                                copy(legacyResume.get("lastBusinessResponse"))
                        );
        TaskWorkflowExecutionSnapshot.TerminalOutcome terminal =
                legacyTerminal(legacy, state, decision);
        return new TaskWorkflowExecutionSnapshot(
                TaskWorkflowExecutionSnapshot.CURRENT_SCHEMA_VERSION,
                new TaskWorkflowExecutionSnapshot.ExecutionIdentity(
                        requiredText(legacy, "executionId"),
                        nullableLong(legacy.get("processId")),
                        requiredText(legacy, "gatewayServiceVersion")
                ),
                new TaskWorkflowExecutionSnapshot.PlanIdentity(
                        requiredText(legacy, "serviceCode"),
                        requiredText(legacy, "inboundAction"),
                        requiredText(legacy, "actionPlanName"),
                        requiredText(legacy, "definitionId"),
                        requiredText(legacy, "planFingerprint")
                ),
                new TaskWorkflowExecutionSnapshot.ExecutionStatus(
                        state,
                        decision,
                        nullableText(legacy.get("activeAttemptId")),
                        nullableInteger(legacy.get("activeStepIndex"))
                ),
                steps,
                resume,
                terminal,
                requiredInstant(legacy, "createdAt"),
                requiredInstant(legacy, "updatedAt")
        );
    }

    private TaskWorkflowExecutionSnapshot.TerminalOutcome legacyTerminal(
            JsonNode legacy,
            TaskWorkflowExecutionState state,
            TaskWorkflowExecutionDecision decision
    ) {
        if (state == TaskWorkflowExecutionState.RUNNING) {
            return null;
        }
        if (decision == TaskWorkflowExecutionDecision.SUCCESS
                || decision == TaskWorkflowExecutionDecision.RETRY_LATER) {
            JsonNode response = legacy.get("storedResponse");
            if (response == null || response.isNull()) {
                throw invalid(
                        "Legacy terminal workflow snapshot has no response"
                );
            }
            return new TaskWorkflowExecutionSnapshot.TerminalOutcome(
                    new TaskWorkflowExecutionSnapshot.StoredResponse(
                            requiredEnum(
                                    response,
                                    "status",
                                    MessageStatus.class
                            ),
                            copy(response.get("payload"))
                    ),
                    null
            );
        }
        if (decision == TaskWorkflowExecutionDecision.FAIL) {
            JsonNode failure = legacy.get("storedFailure");
            if (failure == null || failure.isNull()) {
                throw invalid(
                        "Legacy failed workflow snapshot has no failure"
                );
            }
            return new TaskWorkflowExecutionSnapshot.TerminalOutcome(
                    null,
                    new TaskWorkflowExecutionSnapshot.StoredFailure(
                            nullableInteger(failure.get("stepIndex")),
                            enumValue(
                                    failure.get("messageStatus"),
                                    MessageStatus.class
                            ),
                            nullableText(failure.get("reasonCode")),
                            nullableText(failure.get("reasonMessage")),
                            nullableText(failure.get("normalizedOutcome"))
                    )
            );
        }
        throw invalid("Legacy terminal workflow snapshot decision is invalid");
    }

    private TaskWorkflowAttemptConflictException attemptConflict(
            TaskWorkflowExecutionSnapshot snapshot,
            String reason
    ) {
        return new TaskWorkflowAttemptConflictException(
                "Concurrent TASK_WORKFLOW attempt rejected executionId="
                        + snapshot.execution().executionId() + ": " + reason
        );
    }

    private String requiredText(JsonNode parent, String field) {
        String value = nullableText(parent.get(field));
        if (value == null) {
            throw invalid("Workflow snapshot field=" + field + " is required");
        }
        return value;
    }

    private int requiredInt(JsonNode parent, String field) {
        JsonNode value = parent.get(field);
        if (value == null || !value.canConvertToInt()) {
            throw invalid("Workflow snapshot field=" + field + " is invalid");
        }
        return value.intValue();
    }

    private Instant requiredInstant(JsonNode parent, String field) {
        Instant value = instant(parent.get(field));
        if (value == null) {
            throw invalid("Workflow snapshot field=" + field + " is required");
        }
        return value;
    }

    private <E extends Enum<E>> E requiredEnum(
            JsonNode parent,
            String field,
            Class<E> enumType
    ) {
        E value = enumValue(parent.get(field), enumType);
        if (value == null) {
            throw invalid("Workflow snapshot field=" + field + " is required");
        }
        return value;
    }

    private <E extends Enum<E>> E enumValue(
            JsonNode value,
            Class<E> enumType
    ) {
        String text = nullableText(value);
        return text == null ? null : Enum.valueOf(enumType, text);
    }

    private String nullableText(JsonNode value) {
        if (value == null || value.isNull() || !value.isValueNode()) {
            return null;
        }
        return StringUtils.trimToNull(value.asText());
    }

    private Long nullableLong(JsonNode value) {
        return value == null || value.isNull() ? null : value.longValue();
    }

    private Integer nullableInteger(JsonNode value) {
        return value == null || value.isNull() ? null : value.intValue();
    }

    private Instant instant(JsonNode value) {
        String text = nullableText(value);
        return text == null ? null : Instant.parse(text);
    }

    private JsonNode copy(JsonNode value) {
        return value == null || value.isNull() ? null : value.deepCopy();
    }

    private TaskWorkflowRecoveryException invalid(String message) {
        return new TaskWorkflowRecoveryException(message);
    }
}
