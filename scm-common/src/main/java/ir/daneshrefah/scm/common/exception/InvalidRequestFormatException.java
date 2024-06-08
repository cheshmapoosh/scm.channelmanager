package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class InvalidRequestFormatException extends AbstractValidationException {

    private final String message;

    public InvalidRequestFormatException(String source) {
        this(source, null, null);
    }

    public InvalidRequestFormatException(String source, Throwable cause) {
        this(source, cause.getMessage(), cause);
    }

    public InvalidRequestFormatException(String source, String message, Throwable cause) {
        super(source, message, cause);
        this.message = message;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("source",getSource())
                .defineMessageParameter("message",message)
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
