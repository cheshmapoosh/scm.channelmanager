package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOT_SUPPORT_DATA;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-21
 */
public class MethodNotSupportedException extends AbstractValidationException {

    public MethodNotSupportedException(String source) {
        super(source, source);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOT_SUPPORT_DATA;
    }
}
