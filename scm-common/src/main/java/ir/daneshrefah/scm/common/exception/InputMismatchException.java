package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-21
 */
public class InputMismatchException extends AbstractValidationException {
    private final int parameterCount;
    private final int inputCount;

    public InputMismatchException(int parameterCount, int inputCount) {
        super("mismatch input count", parameterCount + " input required, but " + inputCount + " received.");
        this.parameterCount = parameterCount;
        this.inputCount = inputCount;
    }

    public InputMismatchException(int parameterCount, int inputCount,Exception cause) {
        super("mismatch input count", parameterCount + " input required, but " + inputCount + " received.",cause);
        this.parameterCount = parameterCount;
        this.inputCount = inputCount;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("parameterCount",String.valueOf(parameterCount))
                .defineMessageParameter("inputCount",String.valueOf(inputCount))
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
