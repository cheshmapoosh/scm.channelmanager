package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-25
 */
public class OtpNotFoundException extends BaseOtpException {

    public OtpNotFoundException() {
        super("otp not found.", null);
    }

    @Override
    public String getSource() {
        return null;
    }
}
