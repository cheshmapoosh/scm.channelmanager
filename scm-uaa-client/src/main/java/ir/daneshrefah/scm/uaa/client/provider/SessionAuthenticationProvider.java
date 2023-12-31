package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.SessionAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class SessionAuthenticationProvider extends AbstractClientAuthenticationProvider {


    protected SessionAuthenticationProvider(SessionCache sessionCache) {
        super(sessionCache);
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        SessionAuthenticationToken authenticationToken = (SessionAuthenticationToken) authentication;
        String terminalCode = authenticationToken.getTerminalCode();
        String sessionId = authenticationToken.getSessionId();
        UserAuthentication userAuthentication = getSessionCache().getSessionFromCache(username, terminalCode);
        return userAuthentication;
    }

    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SessionAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
