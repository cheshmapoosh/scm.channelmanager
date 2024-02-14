package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_INPUT_IS_INVALID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class InvalidInputException extends AbstractValidationException {

    public InvalidInputException(String source) {
        super(source, source + " is invalid.");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_INPUT_IS_INVALID;
    }
}
