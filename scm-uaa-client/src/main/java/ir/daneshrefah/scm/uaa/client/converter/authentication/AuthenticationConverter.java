package ir.daneshrefah.scm.uaa.client.converter.authentication;

import ir.daneshrefah.scm.uaa.client.core.ClientAuthenticationRequest;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-21
 */
public interface AuthenticationConverter {

    public BaseTerminalAuthenticationToken convertByRequest(ClientAuthenticationRequest request);

}
