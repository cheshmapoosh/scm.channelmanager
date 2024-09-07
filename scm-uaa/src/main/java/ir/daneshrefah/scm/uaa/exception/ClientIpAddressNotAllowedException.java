package ir.daneshrefah.scm.uaa.exception;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-12
 */
public class ClientIpAddressNotAllowedException extends BaseAuthenticationException {

    public ClientIpAddressNotAllowedException() {
        super("client ip address not allowed.", null);
    }

    public ClientIpAddressNotAllowedException(String message) {
        super(message, null);
    }


    @Override
    public String getErrorCode() {
        return "client_ip_address_not_allowed";
    }

}
