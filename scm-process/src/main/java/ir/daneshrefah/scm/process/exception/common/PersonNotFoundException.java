package ir.daneshrefah.scm.process.exception.common;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import ir.daneshrefah.scm.process.exception.AbstractProcessException;

public class PersonNotFoundException extends AbstractProcessException {

    private String id;

    public PersonNotFoundException(String source, String message, String id) {
        super(source, message);
        this.id = id;
    }

    public PersonNotFoundException(String source, String message, Throwable cause,String id) {
        super(source, message, cause);
        this.id = id;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("id",id)
                .buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}
