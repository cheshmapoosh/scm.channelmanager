package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class DuplicatedRecordFoundException extends AbstractValidationException {

    private final String source;
    public DuplicatedRecordFoundException(String source) {
        super(  source + "' is duplicated.", null);
        this.source = source;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("source",getSource())
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }

}
