package ir.daneshrefah.scm.process.exception.processInstance;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class ProcessInstanceNotFoundWithKeyException extends AbstractProcessException {

    private final String processKey;

    public ProcessInstanceNotFoundWithKeyException(String source, String message, String processKey) {
        super(source, message);
        this.processKey = processKey;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("processKey", processKey)
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
