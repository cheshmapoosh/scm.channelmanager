package ir.daneshrefah.scm.provider.task.workflow;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalLong;

/**
 * Durable task-workflow recovery contract. It intentionally exposes only
 * provider-owned persistence DTOs and stable JDK types.
 */
public interface TaskWorkflowRecoveryStore {

    OptionalLong findProcessIdByClientCorrelation(
            String serviceCode,
            String scmClientCorrelationId
    );

    OptionalLong findProcessIdByTaskId(long taskId);

    Optional<String> findProcessCorrelationId(long processId);

    Map<Long, String> findExecutionIdsByProcessIds(
            Collection<Long> processIds
    );

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
