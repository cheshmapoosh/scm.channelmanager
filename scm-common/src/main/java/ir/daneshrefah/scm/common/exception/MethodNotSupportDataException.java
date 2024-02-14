package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INPUT_IS_INVALID;
import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_NOT_SUPPORT_DATA;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class MethodNotSupportDataException extends AbstractValidationException {

    public MethodNotSupportDataException(String source) {
        super(source, source);
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_NOT_SUPPORT_DATA;
    }
}
