package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_DUPLICATE_RECORD;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class InputAlreadyExistException extends AbstractValidationException {

    public InputAlreadyExistException(String source) {
        super(source, source + " already exist.");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_DUPLICATE_RECORD;
    }
}
