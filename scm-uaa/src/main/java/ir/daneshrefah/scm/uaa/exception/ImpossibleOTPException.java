package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class ImpossibleOTPException extends BaseOtpException {

    public ImpossibleOTPException(String message) {
        super(message, null);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance().buildWithStatus(MessageStatus.SC_NOT_FOUND);
    }
}