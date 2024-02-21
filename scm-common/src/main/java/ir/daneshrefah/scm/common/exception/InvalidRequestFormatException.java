package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INVALID_REQUEST_FORMAT;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOT_SUPPORT_DATA;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class InvalidRequestFormatException extends AbstractValidationException {

    public InvalidRequestFormatException(String source) {
        this(source, null, null);
    }

    public InvalidRequestFormatException(String source, Throwable cause) {
        this(source, cause.getMessage(), cause);
    }

    public InvalidRequestFormatException(String source, String message, Throwable cause) {
        super(source, message, cause);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_INVALID_REQUEST_FORMAT;
    }
}
