package ir.daneshrefah.scm.core.integration.gateway.taskworkflow;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class TaskWorkflowRouteIdentityException extends AbstractBaseException {

    public TaskWorkflowRouteIdentityException(String message) {
        super(message, null);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder.createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
