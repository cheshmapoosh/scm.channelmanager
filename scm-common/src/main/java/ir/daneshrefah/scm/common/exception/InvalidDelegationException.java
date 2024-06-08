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
 * @since 2024-05-19
 */
public class InvalidDelegationException extends AbstractBaseException implements ExceptionSourceAware {

    private final String username;

    public InvalidDelegationException(String username) {
        this(username, null);
    }

    public InvalidDelegationException(String username, Throwable cause) {
        super("user: " + username + ", hasn't delegation authority.", cause);
        this.username= username;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("username",getSource())
                .buildWithStatus(MessageStatus.SC_ACCESS_DENIED);
    }

    @Override
    public String getSource() {
        return username;
    }
}
