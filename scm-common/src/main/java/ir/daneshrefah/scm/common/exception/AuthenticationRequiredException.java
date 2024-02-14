package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_AUTHENTICATION_REQUIRED;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class AuthenticationRequiredException extends AbstractValidationException {

    public AuthenticationRequiredException() {
        super("authentication", "authentication required.");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_AUTHENTICATION_REQUIRED;
    }
}
