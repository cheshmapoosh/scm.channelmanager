package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class BearerAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {

    private final JwtDecoder jwtDecoder;
    private final JwtTokenConverter jwtTokenConverter;

    public BearerAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                        JwtDecoder jwtDecoder,
                                        SessionCache sessionCache) {
        super(remoteSecurityServiceProvider, sessionCache);
        this.jwtDecoder = jwtDecoder;
        this.jwtTokenConverter = new JwtTokenConverter();
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        BearerAuthenticationToken bearer = (BearerAuthenticationToken) authentication;
        Jwt jwt = jwtDecoder.decode(bearer.getToken());
        UserAuthentication userAuthentication = jwtTokenConverter.convert(jwt);
        validateUserAuthentication(authentication, userAuthentication);
        if (StringUtils.isNotEmpty(userAuthentication.getDetails().getSessionId())) {
            String tokenUsername = userAuthentication.getName(); //TODO username
            String tokenTerminalCode = userAuthentication.getPrincipal().getTerminalCode();
            userAuthentication = getSessionCache().getSessionFromCache(tokenUsername, tokenTerminalCode);
            if (null == userAuthentication) {
                throw new SessionAuthenticationException("invalid session id for user: " + tokenUsername);
            }
        }

        return userAuthentication;
    }

    private void validateUserAuthentication(BaseAuthenticationToken authentication, UserAuthentication userAuthentication) {
        if (null == userAuthentication) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
    }


    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {
        String requestClientId = ((BaseTerminalAuthenticationToken) authentication).getClientId();
        String authenticationClientId = userAuthentication.getDetails().getClientId();
        String requestTerminalCode = ((BaseTerminalAuthenticationToken) authentication).getTerminalCode();
        String authenticationTerminalCode = userAuthentication.getPrincipal().getTerminalCode();
//        if (!StringUtils.equalsIgnoreCase(requestTerminalCode, authenticationTerminalCode)) {
//            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_TERMINAL);
//        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
