package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class TaskWorkflowSnapshotUnavailableException
        extends TaskWorkflowExecutionException {

    public TaskWorkflowSnapshotUnavailableException(
            String executionId,
            Long processId,
            String serviceCode,
            String inboundAction,
            String reason
    ) {
        super(
                "TASK_WORKFLOW snapshot is unavailable executionId="
                        + executionId + ", processId=" + processId
                        + ", serviceCode=" + serviceCode
                        + ", inboundAction=" + inboundAction
                        + ", reason=" + reason,
                MessageStatus.SC_ERROR_SYSTEM,
                null
        );
    }
}
