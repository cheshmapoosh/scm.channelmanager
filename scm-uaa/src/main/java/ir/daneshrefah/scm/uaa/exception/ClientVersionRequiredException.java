package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
public class ClientVersionRequiredException extends BaseAuthenticationException {

    public ClientVersionRequiredException() {
        super("client version not sent.", null);
    }

    @Override
    public String getErrorCode() {
        return "empty_client_version";
    }

}
