package ir.daneshrefah.scm.uaa.exception;

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
    public String getSource() {
        return null;
    }
}
