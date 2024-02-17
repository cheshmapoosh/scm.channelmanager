package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
public class ActivationCodeRequiredException extends BaseAuthenticationException {

    public ActivationCodeRequiredException() {
        super("activation code not sent.", null);
    }

    @Override
    public String getErrorCode() {
        return "empty_activation_code";
    }

}
