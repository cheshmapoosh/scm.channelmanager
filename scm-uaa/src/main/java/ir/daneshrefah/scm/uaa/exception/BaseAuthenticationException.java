package ir.daneshrefah.scm.uaa.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
public abstract class BaseAuthenticationException extends AuthenticationException {

    public BaseAuthenticationException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public abstract String getErrorCode();

}
