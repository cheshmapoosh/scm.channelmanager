package ir.daneshrefah.scm.uaa.client;

import ir.daneshrefah.scm.common.exception.BaseException;
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
public class ClientAuthenticationException extends BaseException {

    private final UserAuthentication authentication;

    public ClientAuthenticationException(String message, Throwable cause, UserAuthentication authentication) {
        super(message, cause);
        this.authentication = authentication;
    }

    @Override
    public String getSource() {
        return Constants.SCM_PARAMETER_AUTHENTICATION;
    }
}
