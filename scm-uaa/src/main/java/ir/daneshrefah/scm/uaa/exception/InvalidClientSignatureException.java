package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-17
 */
public class InvalidClientSignatureException extends BaseAuthenticationException {

    public InvalidClientSignatureException() {
        super("invalid client signature.", null);
    }

    @Override
    public String getErrorCode() {
        return "invalid_client_signature";
    }
}
