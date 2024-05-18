package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-12
 */
public class ClientCodeRequiredException extends BaseAuthenticationException {

    public ClientCodeRequiredException() {
        super("client code not sent.", null);
    }

    @Override
    public String getErrorCode() {
        return "empty_client_code";
    }

}
