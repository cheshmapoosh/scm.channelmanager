package ir.daneshrefah.scm.process.exception.processInstance;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class ProcessInstanceNotFoundException extends AbstractProcessException {

    private final String processInstanceId;

    public ProcessInstanceNotFoundException(String source, String message, String processInstanceId) {
        super(source, message);
        this.processInstanceId = processInstanceId;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("processInstanceId", processInstanceId)
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
