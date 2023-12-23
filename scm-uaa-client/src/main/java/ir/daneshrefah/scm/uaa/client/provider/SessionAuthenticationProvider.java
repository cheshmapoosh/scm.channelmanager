package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.SessionAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class SessionAuthenticationProvider extends AbstractClientAuthenticationProvider {


    @Override
    protected UserDetails retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        return null;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SessionAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
