package ir.daneshrefah.scm.uaa.exception;

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

    @Override
    public String getSource() {
        return null;
    }
}
