package ir.daneshrefah.scm.core.integration.service.routing.taskworkflow;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public abstract class TaskWorkflowExecutionException extends AbstractBaseException {
    private final MessageStatus status;

    protected TaskWorkflowExecutionException(
            String message,
            MessageStatus status,
            Throwable cause
    ) {
        super(message, cause);
        this.status = status;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder.createInstance()
                .buildWithStatus(status);
    }
}
