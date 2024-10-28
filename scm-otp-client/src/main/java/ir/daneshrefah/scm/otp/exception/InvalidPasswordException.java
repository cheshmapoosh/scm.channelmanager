package ir.daneshrefah.scm.otp.exception;


public class InvalidPasswordException extends AbstractOtpException {

    public InvalidPasswordException(String source, String message) {
        super(source, message);
    }
}