package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.SessionAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.USERNAME_NONE_PROVIDED;

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
        SessionAuthenticationToken sessionAuthenticationToken = (SessionAuthenticationToken) authentication;
        String terminalCode = sessionAuthenticationToken.getTerminalCode();
        String sessionId = sessionAuthenticationToken.getSessionId();
        if (StringUtils.isEmpty(username) || StringUtils.equalsIgnoreCase(USERNAME_NONE_PROVIDED, username)) {
            throw new UsernameNotFoundException("empty username for sessionId: " + sessionId);
        }
        UserAuthentication userAuthentication = getSessionCache().getSessionFromCache(username, terminalCode);
        if (null == userAuthentication) {
            throw new SessionAuthenticationException(String.format("invalid sessionId %s for user %s.",
                    sessionId, username));
        }
        if (!StringUtils.equals(sessionId, userAuthentication.getDetails().getSessionId())) {
            throw new SessionAuthenticationException(String.format("invalid sessionId %s for user %s.",
                    sessionId, username));
        }
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
