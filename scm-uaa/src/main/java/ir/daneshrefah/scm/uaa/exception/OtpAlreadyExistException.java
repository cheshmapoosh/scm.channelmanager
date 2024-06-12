package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;
import ir.daneshrefah.scm.common.error.ExceptionInformationBuilder;
import ir.daneshrefah.scm.common.model.message.MessageStatus;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-21
 */
public class OtpAlreadyExistException extends BaseOtpException {

    public OtpAlreadyExistException() {
        super("otp already exist.", null);
    }


    @Override
    public ExceptionInformation getExceptionInformation() {
        return ExceptionInformationBuilder
                .createInstance().buildWithStatus(MessageStatus.SC_ERROR_VALIDATION);
    }
}
