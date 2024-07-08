package ir.daneshrefah.scm.process.exception.definition;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class ProcessDefinitionExistsException extends AbstractProcessException {

    private final String deploymentId;

    public ProcessDefinitionExistsException(String source, String message, String deploymentId) {
        super(source, message);
        this.deploymentId = deploymentId;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("deploymentId", deploymentId)
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
