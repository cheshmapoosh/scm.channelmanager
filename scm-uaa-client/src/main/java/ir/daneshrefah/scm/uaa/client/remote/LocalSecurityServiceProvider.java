package ir.daneshrefah.scm.uaa.client.remote;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.client.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-04-20
 */
@Component
@ConditionalOnProperty(name = "scm.security.distributed", havingValue = "false", matchIfMissing = true)
public class LocalSecurityServiceProvider implements SecurityServiceProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    public String authenticateClaim(ClaimAuthenticationToken authentication) throws AuthenticationException {
        return null;
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
