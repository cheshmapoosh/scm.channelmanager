package ir.daneshrefah.scm.uaa.common.exception;

import lombok.Getter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-07
 */
public class TwoStepAuthenticationRequiredException extends AuthenticationException {

    @Getter
    private final Authentication authentication;

    public TwoStepAuthenticationRequiredException(Authentication authentication) {
        this(authentication, null);
    }

    public TwoStepAuthenticationRequiredException(Authentication authentication, Exception cause) {
        super("two step required.", cause);
        this.authentication = authentication;
    }

}
