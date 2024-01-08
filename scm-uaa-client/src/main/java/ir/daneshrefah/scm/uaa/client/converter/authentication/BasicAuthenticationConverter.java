package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BasicAuthenticationConverter extends org.springframework.security.web.authentication.www.BasicAuthenticationConverter
        implements AuthenticationConverter {

    @Override
    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request) {
        return null;
    }

}
