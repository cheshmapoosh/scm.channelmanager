package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class TaskWorkflowPersistenceException
        extends TaskWorkflowExecutionException {

    public TaskWorkflowPersistenceException(String message) {
        this(message, null);
    }

    public TaskWorkflowPersistenceException(String message, Throwable cause) {
        super(message, MessageStatus.SC_ERROR_SYSTEM, cause);
    }
}
