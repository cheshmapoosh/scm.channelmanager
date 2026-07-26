package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import java.util.Optional;
import java.util.OptionalLong;

public interface TaskWorkflowRecoveryStore {

    OptionalLong findProcessIdByExecutionId(String executionId);

    OptionalLong findProcessIdByTaskId(long taskId);

    Optional<RoutingExecutionSnapshot> loadForUpdate(
            long processId,
            String inboundAction,
            String gatewayServiceVersion
    );

    RoutingExecutionSnapshot registerAttempt(
            long processId,
            RoutingExecutionSnapshot snapshot,
            String expectedAttemptId
    );

    void saveProgress(long processId, RoutingExecutionSnapshot snapshot);

    void saveRetryLater(long processId, RoutingExecutionSnapshot snapshot);

    void saveCompleted(long processId, RoutingExecutionSnapshot snapshot);

    void saveFailed(long processId, RoutingExecutionSnapshot snapshot);
}
