package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class InvalidTaskWorkflowExecutionStateException
        extends TaskWorkflowExecutionException {

    public InvalidTaskWorkflowExecutionStateException(String message) {
        super(message, MessageStatus.SC_ERROR_VALIDATION, null);
    }
}
