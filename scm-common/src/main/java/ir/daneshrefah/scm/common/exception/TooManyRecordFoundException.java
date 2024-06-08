package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-14
 */
public class TooManyRecordFoundException extends AbstractValidationException {

    final int count;
    public TooManyRecordFoundException(String source, int count) {
        super(source, "too many (" + count + ") " + source + " found.");
        this.count = count;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("count",String.valueOf(this.count))
                .defineMessageParameter("source",getSource())
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
