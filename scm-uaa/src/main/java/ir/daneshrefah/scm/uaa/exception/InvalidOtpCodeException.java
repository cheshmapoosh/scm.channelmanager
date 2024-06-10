package ir.daneshrefah.scm.uaa.exception;

import ir.daneshrefah.scm.common.error.ExceptionInformation;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-25
 */
public class InvalidOtpCodeException extends BaseOtpException {

    public InvalidOtpCodeException() {
        super("invalid otp code.", null);
    }
    public InvalidOtpCodeException(String message) {
        super(message, null);
    }

    @Override
    public ExceptionInformation getExceptionInformation() {
        return null;
    }
}
