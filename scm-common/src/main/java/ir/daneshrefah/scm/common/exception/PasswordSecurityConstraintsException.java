package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class PasswordSecurityConstraintsException extends AbstractBaseException implements ExceptionSourceAware {

    private final String source;

    public PasswordSecurityConstraintsException(String source) {
        this(source, "input password does not have require security constraints", null);
    }

    public PasswordSecurityConstraintsException(String source, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }

    @Override
    public String getSource() {
        return source;
    }
}
