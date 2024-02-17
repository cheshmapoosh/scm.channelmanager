package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
public class InvalidClientVersionException extends BaseAuthenticationException {

    public InvalidClientVersionException(String version) {
        super("invalid client version '" + version + "'.", null);
    }

    @Override
    public String getErrorCode() {
        return "invalid_client_version";
    }
}
