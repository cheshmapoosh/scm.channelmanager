package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.common.model.person.ClientPerson;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.service.LogoutService;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
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
public class BearerAuthenticationProvider extends AbstractClientAuthenticationProvider {

    private final JwtDecoder jwtDecoder;
    private final JwtTokenConverter jwtTokenConverter;
    private static final String JWT_ID_CACHE_NAME = "jwt:jti";
    @Autowired
    private LogoutService logoutService;

    public BearerAuthenticationProvider(JwtDecoder jwtDecoder,
                                        SessionCache sessionCache,
                                        CacheManager cacheManager) {
        super(sessionCache, cacheManager);
        this.jwtDecoder = jwtDecoder;
        this.jwtTokenConverter = new JwtTokenConverter();
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        BearerAuthenticationToken bearer = (BearerAuthenticationToken) authentication;
        Jwt jwt = jwtDecoder.decode(bearer.getToken());
        UserAuthentication userAuthentication = jwtTokenConverter.convert(jwt, username);
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
        String username = userAuthentication.getPrincipal().getNickname();
//        if (!StringUtils.equalsIgnoreCase(requestTerminalCode, authenticationTerminalCode)) {
//            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_TERMINAL);
//        }
        if (userAuthentication.getPrincipal().getPerson() instanceof ClientPerson){
            return;
        }
        if (authentication instanceof BearerAuthenticationToken bearerAuthenticationToken) {
            validateJwtId(jwtDecoder.decode(bearerAuthenticationToken.getToken()), username, authenticationTerminalCode,bearerAuthenticationToken);
        }
    }

    private void validateJwtId(Jwt jwt, String username, String terminalCode, BearerAuthenticationToken authentication) {
        String jwtTokenId = jwt.getClaim(Constants.CLAIM_KEY_JWT_IDENTIFIER);
        String cacheKey = String.format("%s%s%s", username, "::", terminalCode);
        String cachedTokenId = cachedJwtId(cacheKey);
        if (StringUtils.isBlank(jwtTokenId) || !jwtTokenId.equals(cachedTokenId)) {
            logoutService.sendLogoutMessage(authentication);
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_TOKEN, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
    }

    private String cachedJwtId(String cacheKey) {
        Cache cache = cacheManager().getCache(JWT_ID_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + JWT_ID_CACHE_NAME);
        }
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        return valueWrapper == null ? null : (String) valueWrapper.get();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
