package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class RemoveParentRecordException extends AbstractValidationException {

    private final String source;

    public RemoveParentRecordException(String source) {
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
                .buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
