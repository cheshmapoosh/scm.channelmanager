package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-21
 */
public class OtpCodeGenerationException extends BaseOtpException {

    public OtpCodeGenerationException() {
        super("error on create otp code.", null);
    }

    @Override
    public String getSource() {
        return null;
    }
}
