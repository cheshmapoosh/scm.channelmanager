package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-16
 */
@Getter
public class AccessDeniedException extends AbstractBaseException {

    private final String source;
    private final Integer errorCode;

    public AccessDeniedException(String source, Integer errorCode, String message) {
        super(message, null);
        this.source = source;
        this.errorCode = errorCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance().buildWithStatus(MessageStatus.SC_ACCESS_DENIED);
    }

}
