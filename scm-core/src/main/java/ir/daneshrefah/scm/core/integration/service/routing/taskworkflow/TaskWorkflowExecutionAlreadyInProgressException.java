package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class TaskWorkflowExecutionAlreadyInProgressException
        extends TaskWorkflowExecutionException {

    public TaskWorkflowExecutionAlreadyInProgressException(String lockName) {
        super(
                "TASK_WORKFLOW execution already in progress; retryable=true, "
                        + "lock=" + lockName,
                MessageStatus.SC_ERROR_UNREACHABLE_PROVIDER,
                null
        );
    }
}
