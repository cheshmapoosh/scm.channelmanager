package ir.daneshrefah.scm.provider.task.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class ProcessAuthorityException extends AbstractProcessException {
    public ProcessAuthorityException(String source, String message) {
        super(source, message);
    }

    public ProcessAuthorityException(String source, String message, Throwable cause) {
        super(source, message, cause);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ACCESS_DENIED);
    }
}