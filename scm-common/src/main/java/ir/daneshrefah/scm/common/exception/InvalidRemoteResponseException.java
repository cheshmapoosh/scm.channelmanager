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
public class InvalidRemoteResponseException extends AbstractValidationException {

    private final String providerCode;

    public InvalidRemoteResponseException(String providerCode, String source) {
        this(providerCode, source, null);
    }

    public InvalidRemoteResponseException(String providerCode, String source, Throwable cause) {
        super(source, providerCode + "(" + source + ")" + " is invalid.", cause);
        this.providerCode = providerCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("source",getSource())
                .defineMessageParameter("providerCode",providerCode)
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }

}
