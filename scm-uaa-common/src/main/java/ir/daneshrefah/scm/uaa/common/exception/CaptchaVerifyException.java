package ir.daneshrefah.scm.uaa.common.exception;

import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-11
 */
public class CaptchaVerifyException extends AuthenticationException {

    public CaptchaVerifyException() {
        super("mismatch captcha");
    }

}
