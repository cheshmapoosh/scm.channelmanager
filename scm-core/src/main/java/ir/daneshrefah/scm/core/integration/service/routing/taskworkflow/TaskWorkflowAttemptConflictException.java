package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class TaskWorkflowAttemptConflictException
        extends TaskWorkflowExecutionException {

    public TaskWorkflowAttemptConflictException(String message) {
        super(message, MessageStatus.SC_ERROR_DATA_INTEGRITY_VIOLATION, null);
    }
}
