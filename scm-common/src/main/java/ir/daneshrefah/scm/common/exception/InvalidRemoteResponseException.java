package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INPUT_IS_INVALID;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INVALID_REMOTE_RESPONSE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class InvalidRemoteResponseException extends AbstractValidationException {

    private final String providerCode;

    public InvalidRemoteResponseException(String providerCode, String source) {
        this(providerCode, source, null);
    }

    public InvalidRemoteResponseException(String providerCode, String source, Throwable cause) {
        super(source, providerCode + "(" + source + ")" + " is invalid.", cause);
        this.providerCode = providerCode;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_INVALID_REMOTE_RESPONSE;
    }
}
