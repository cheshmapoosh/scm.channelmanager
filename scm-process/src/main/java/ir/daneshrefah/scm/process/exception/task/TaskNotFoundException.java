package ir.daneshrefah.scm.process.exception.task;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class TaskNotFoundException extends AbstractProcessException {

    public TaskNotFoundException(String source, String message) {
        super(source, message);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
