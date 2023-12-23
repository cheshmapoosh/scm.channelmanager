package ir.daneshrefah.scm.uaa.security.authenticationProvider.provider;

import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.Assert;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(GeneralAuthenticationToken.class, authentication,
                () -> "AbstractAuthenticationProvider.onlySupports GeneralAuthenticationToken.");

        TerminalUserDetails userDetails = (TerminalUserDetails) authentication.getDetails();
        logger.debug("start authenticate ");
        return null;
    }
}
