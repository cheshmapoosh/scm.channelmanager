package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

public class PasswordTypeIsNotOtp extends BaseOtpException {

    public PasswordTypeIsNotOtp() {
        super("Password type is not OTP.", null);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance().buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}