package ir.daneshrefah.scm.uaa.common.utils;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.DEFAULT_ERROR_URI;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-31
 */
public class ErrorUtils {

    public static void throwError(String errorCode, String parameterName) {
        throwError(errorCode, parameterName, DEFAULT_ERROR_URI);
    }

    public static void throwError(String errorCode, String parameterName, String errorUri) {
        OAuth2Error error = new OAuth2Error(errorCode, "OAuth 2.0 Parameter: " + parameterName, errorUri);
        throw new OAuth2AuthenticationException(error);
    }

}
