package ir.daneshrefah.scm.common.exception;

import static ir.daneshrefah.scm.common.model.error.ErrorCodes.ERROR_CODE_MISMATCH_REQUIRED_INPUT;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-21
 */
public class InputMismatchException extends AbstractValidationException {

    public InputMismatchException(String source, Exception cause) {
        super(source, cause.getMessage(), cause);
    }

    public InputMismatchException(int parameterCount, int inputCount) {
        super("mismatch input count", parameterCount + " input required, but " + inputCount + " received.");
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_MISMATCH_REQUIRED_INPUT;
    }
}
