package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class RegisterNewUserException extends BaseOtpException {
    private final String errorCode;

    public RegisterNewUserException(String errorCode) {
        super(String.format("user does not save in avacas due to errorCode : '%s'", errorCode), null);
        this.errorCode = errorCode;
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance()
                .defineMessageParameter("errorCode",errorCode)
                .buildWithStatus(MessageStatus.SC_ERROR_SYSTEM);
    }
}
