package ir.daneshrefah.scm.task.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class AbstractProcessException extends AbstractBaseException implements ExceptionSourceAware {

    private final String source;

    public AbstractProcessException(String source, String message) {
        this(source, message, null);
    }

    public AbstractProcessException(String source, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
