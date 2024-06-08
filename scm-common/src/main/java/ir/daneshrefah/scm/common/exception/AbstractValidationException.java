package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public abstract class AbstractValidationException extends AbstractBaseException implements ExceptionSourceAware {

    private final String source;

    public AbstractValidationException(String source, String message) {
        this(source, message, null);
    }

    public AbstractValidationException(String source, String message, Throwable cause) {
        super(message, cause);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

}
