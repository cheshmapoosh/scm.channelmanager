package ir.daneshrefah.scm.uaa.client;

import ir.daneshrefah.scm.common.exception.BaseException;
import ir.daneshrefah.scm.common.model.message.IAuthenticationHeader;
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

    private final IAuthenticationHeader authentication;

    public ClientAuthenticationException(String message, Throwable cause, IAuthenticationHeader authentication) {
        super(message, cause);
        this.authentication = authentication;
    }
}
