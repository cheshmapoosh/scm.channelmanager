package ir.daneshrefah.scm.uaa.client.remote;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.client.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-20
 */
public interface SecurityServiceProvider {

    /**
     * Legacy second-password claim authentication is inactive. Do not use for new integrations.
     */
    @Deprecated(since = "9.0.0", forRemoval = true)
    String authenticateClaim(ClaimAuthenticationToken authentication) throws AuthenticationException;

    String authenticateClient(ClientAuthenticationToken authentication) throws AuthenticationException;

    String authenticateBasic(BasicAuthenticationToken authentication) throws AuthenticationException;

    GeneralPerson findGeneralPersonByUsernameAndTerminalCode(String username, String terminalCode);

}
