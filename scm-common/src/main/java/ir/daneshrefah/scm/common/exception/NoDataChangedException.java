package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_VALIDATION_NO_DATA_CHANGE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class NoDataChangedException extends AbstractValidationException {

    public NoDataChangedException(String source) {
        super(source, source + " data has no change");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_VALIDATION_NO_DATA_CHANGE;
    }
}
