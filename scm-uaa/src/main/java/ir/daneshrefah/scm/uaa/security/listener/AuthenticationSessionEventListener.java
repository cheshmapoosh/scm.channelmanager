package ir.daneshrefah.scm.uaa.security.listener;

import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-27
 */
@Component
public class AuthenticationSessionEventListener extends BaseAuthenticationListener {

    private final SessionCache sessionCache;

    public AuthenticationSessionEventListener(SessionCache sessionCache) {
        this.sessionCache = sessionCache;
    }


    @Override
    protected void onSuccessAuthenticationEvent(PostAuthenticationToken authentication) {
        if (!authentication.isSessionRequired()) {
            return;
        }
        String sessionId = generateSessionId(
                authentication.getName(), authentication.getTerminalCode());
        authentication.setSessionId(sessionId);

        UserAuthentication sessionAuthentication = createUserSessionData(authentication);
        sessionCache.putSessionInCache(sessionAuthentication);
    }

    private UserAuthentication createUserSessionData(PostAuthenticationToken authentication) {
        UserAuthentication.AuthenticationDetail detail = UserAuthentication.AuthenticationDetail.builder()
                .issuer(null)
                .issuedAt(authentication.getIssuedAt())
                .expiresAt(authentication.getExpiresAt())
                .maxIdle(null)
                .loginData(null)
                .loginAccessParameter(null)
                .sessionId(authentication.getSessionId())
                .clientId(authentication.getDetails().getClientId())
                .build();

        UserAuthentication userAuthentication = new UserAuthentication(detail,
                authentication.getPrincipal().getUser(),
                PostAuthenticationToken.AuthenticationStatus.AUTHENTICATED.equals(authentication.getAuthenticationStatus()) ?
                        authentication.getAuthorities() : null);

        return userAuthentication;
    }

    private String generateSessionId(String name, String terminalCode) {
        return UUID.randomUUID().toString();
    }

    private String generateSessionKey(String username, String terminalCode) {
        LOGGER.debug("Generating sessionKey for user: " + username);
        String result = username
                .concat(StringUtils.DOUBLE_COLON)
                .concat(terminalCode);
        return result;
    }

}
