package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-21
 */
public interface AuthenticationConverter {

    public AbstractAuthenticationToken convertByHeader(String username, String terminalCode, String authorizationHeader);

    public AbstractAuthenticationToken convertByRequest(ClientAuthenticationRequest request);

}
