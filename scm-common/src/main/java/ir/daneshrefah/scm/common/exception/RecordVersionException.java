package ir.daneshrefah.scm.common.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.error.spec.AbstractBaseException;
import ir.daneshrefah.scm.common.error.spec.ExceptionSourceAware;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class RecordVersionException extends AbstractBaseException implements ExceptionSourceAware {


    private final String source;
    public RecordVersionException(String source) {
        super("record version does not match", null);
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
                .buildWithStatus(MessageStatus.SC_ERROR_BUSINESS);
    }


}
