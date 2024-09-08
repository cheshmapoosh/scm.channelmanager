package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-12
 */
public class ClientAuthenticationRequiredException extends BaseAuthenticationException {

    public ClientAuthenticationRequiredException() {
        super("client not authenticate.", null);
    }

    @Override
    public String getErrorCode() {
        return "no_client_authenticate";
    }

}
