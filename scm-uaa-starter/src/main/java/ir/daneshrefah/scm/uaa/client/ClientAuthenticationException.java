package ir.daneshrefah.scm.uaa.client;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.constant.Constants;
import lombok.Getter;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-07
 */
@Getter
public class ClientAuthenticationException extends AbstractBaseException implements ExceptionSourceAware {

    private final UserAuthentication authentication;

    public ClientAuthenticationException(String message, Throwable cause, UserAuthentication authentication) {
        super(message, cause);
        this.authentication = authentication;
    }

    @Override
    public String getSource() {
        return Constants.SCM_PARAMETER_AUTHENTICATION;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance().buildWithStatus(MessageStatus.SC_ACCESS_DENIED);
    }
}
