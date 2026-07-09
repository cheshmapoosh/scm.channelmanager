package ir.daneshrefah.scm.uaa.starter.remote;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.starter.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.provider.token.ClientAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-20
 */
public class LocalSecurityServiceProvider implements SecurityServiceProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    @Deprecated(since = "9.0.0", forRemoval = true)
    @SuppressWarnings("removal")
    public String authenticateClaim(ClaimAuthenticationToken authentication) throws AuthenticationException {
        throw new AuthenticationServiceException("second-password claim authentication is inactive");
    }

    @Override
    public String authenticateClient(ClientAuthenticationToken authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public String authenticateBasic(BasicAuthenticationToken authentication) throws AuthenticationException {
        return null;
    }

    @Override
    public GeneralPerson findGeneralPersonByUsernameAndTerminalCode(String username, String terminalCode) {
        return null;
    }

}
