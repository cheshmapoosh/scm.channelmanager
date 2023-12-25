package ir.daneshrefah.scm.uaa.client.converter.authentication;

import org.springframework.security.authentication.AbstractAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-21
 */
public interface AuthenticationConverter {

    public AbstractAuthenticationToken convertByHeader(String terminalCode, String authorizationHeader);
    
}
