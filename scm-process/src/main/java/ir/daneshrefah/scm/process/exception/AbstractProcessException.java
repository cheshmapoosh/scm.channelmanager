package ir.daneshrefah.scm.process.exception;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.exception.ErrorCodeAwareException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public abstract class AbstractProcessException extends BaseException implements ErrorCodeAwareException {

    private final String source;

    public AbstractProcessException(String source, String message) {
        this(source, message, null);
    }

    public AbstractProcessException(String source, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    @Override
    public MessageStatus getStatus() {
        return MessageStatus.SC_ERROR_VALIDATION;
    }

    @Override
    public String getSource() {
        return source;
    }
}
