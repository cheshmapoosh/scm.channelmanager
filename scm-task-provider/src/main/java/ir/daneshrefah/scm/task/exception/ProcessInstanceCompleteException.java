package ir.daneshrefah.scm.task.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class ProcessInstanceCompleteException extends AbstractProcessException {
    public ProcessInstanceCompleteException(String source, String message) {
        super(source, message);
    }

    public ProcessInstanceCompleteException(String source, String message, Throwable cause) {
        super(source, message, cause);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
