package ir.daneshrefah.scm.process.exception.task;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class InvalidAssigneeException extends AbstractProcessException {

    public InvalidAssigneeException(String source, String message) {
        super(source, message);
    }

    public InvalidAssigneeException(String source, String message, Throwable cause) {
        super(source, message, cause);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_UNAUTHORIZED);
    }
}