package ir.daneshrefah.scm.provider.task.workflow;

import java.util.Optional;
import java.util.OptionalLong;

/**
 * Durable task-workflow recovery contract. It intentionally exposes only
 * provider-owned persistence DTOs and stable JDK types.
 */
public interface TaskWorkflowRecoveryStore {

    /**
     * Resolves a process only when its persisted correlation value exactly
     * equals {@code correlationId}.
     */
    OptionalLong findProcessIdByCorrelationId(String correlationId);

    OptionalLong findProcessIdByTaskId(long taskId);

    Optional<TaskWorkflowExecutionSnapshot> loadForUpdate(long processId);

    TaskWorkflowExecutionSnapshot registerAttempt(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot,
            String expectedAttemptId
    );

    void saveProgress(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    );

    void saveRetryLater(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    );

    void saveCompleted(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    );

    void saveFailed(
            long processId,
            TaskWorkflowExecutionSnapshot snapshot
    );
}
