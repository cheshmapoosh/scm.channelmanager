package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_TOO_MANY_RECORD_FOUND;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class TooManyRecordFoundException extends AbstractValidationException {

    public TooManyRecordFoundException(String source, int count) {
        super(source, "too many (" + count + ") " + source + " found.");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_TOO_MANY_RECORD_FOUND;
    }
}
