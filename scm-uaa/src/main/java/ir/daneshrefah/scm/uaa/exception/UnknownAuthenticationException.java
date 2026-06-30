package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-18
 */
public class UnknownAuthenticationException extends BaseAuthenticationException {

    public UnknownAuthenticationException(Exception cause) {
        super("unknown authentication exception", cause);
    }

    @Override
    public String getErrorCode() {
        return "unknown_authentication_exception";
    }
}
