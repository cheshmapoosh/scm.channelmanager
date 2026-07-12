package ir.daneshrefah.scm.uaa.starter.provider;

import ir.daneshrefah.scm.common.model.person.ClientPerson;
import ir.daneshrefah.scm.uaa.starter.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.service.LogoutService;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BearerAuthenticationProvider extends AbstractClientAuthenticationProvider {

    private final JwtDecoder jwtDecoder;
    private final JwtTokenConverter jwtTokenConverter;
    private static final String JWT_ID_CACHE_NAME = "jwt:jti";
    @Autowired(required = false)
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
        Jwt jwt = validatedJwt(bearer);
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

        return detachedAuthentication(userAuthentication, null);
    }


    private Jwt validatedJwt(BearerAuthenticationToken bearer) {
        Jwt validatedJwt = bearer.getValidatedJwt();
        if (validatedJwt != null) {
            return validatedJwt;
        }
        try {
            validatedJwt = jwtDecoder.decode(bearer.getToken());
            bearer.validatedJwt(validatedJwt);
            return validatedJwt;
        } catch (JwtException ex) {
            throw new InvalidBearerTokenException("Bearer token is invalid or expired", ex);
        }
    }
    private void validateUserAuthentication(BaseAuthenticationToken authentication, UserAuthentication userAuthentication) {
        if (null == userAuthentication) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
    }


    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {
        String authenticationTerminalCode = userAuthentication.getPrincipal().getTerminalCode();
        String username = userAuthentication.getPrincipal().getNickname();
        if (userAuthentication.getPrincipal().getPerson() instanceof ClientPerson){
            return;
        }
        if (authentication instanceof BearerAuthenticationToken bearerAuthenticationToken) {
            Jwt jwt = validatedJwt(bearerAuthenticationToken);
            validateJwtId(jwt, username, authenticationTerminalCode, bearerAuthenticationToken);
        }
    }

    private void validateJwtId(Jwt jwt, String username, String terminalCode, BearerAuthenticationToken authentication) {
        String jwtTokenId = jwt.getClaim(Constants.CLAIM_KEY_JWT_IDENTIFIER);
        String cacheKey = String.format("%s%s%s", username, "::", terminalCode);
        String cachedTokenId = cachedJwtId(cacheKey);
        if (StringUtils.isBlank(jwtTokenId) || !jwtTokenId.equals(cachedTokenId)) {
            if(logoutService != null) {
                logoutService.sendLogoutMessage(authentication);
            }
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
    protected UserAuthentication createSuccessAuthentication(UserAuthentication user, Authentication authentication) {
        if (!(authentication instanceof BearerAuthenticationToken bearer) || bearer.getValidatedJwt() == null) {
            return user;
        }
        return detachedAuthentication(user, bearer.getValidatedJwt());
    }

    private UserAuthentication detachedAuthentication(UserAuthentication user, Jwt loginData) {
        UserAuthentication.AuthenticationDetail details = user == null ? null : user.getDetails();
        UserAuthentication.AuthenticationDetail safeDetails = UserAuthentication.AuthenticationDetail.builder()
                .issuer(details == null ? null : details.getIssuer())
                .issuedAt(details == null ? null : details.getIssuedAt())
                .expiresAt(details == null ? null : details.getExpiresAt())
                .maxIdle(details == null ? null : details.getMaxIdle())
                .loginData(loginData)
                .loginAccessParameter(details == null ? null : details.getLoginAccessParameter())
                .sessionId(details == null ? null : details.getSessionId())
                .clientId(details == null ? null : details.getClientId())
                .build();
        String delegatedUsername = user != null && user.isDelegated()
                ? user.getProfile().getNickname()
                : null;
        UserAuthentication copy = new UserAuthentication(
                safeDetails,
                user == null ? null : user.getPrincipal(),
                delegatedUsername,
                user == null ? null : user.getAuthorities()
        );
        if (user != null) {
            copy.setAuthenticated(user.isAuthenticated());
            copy.setError(user.getError());
            if (user.getIsTransactionAuthenticated() != null) {
                copy.authenticateTransaction(user.getIsTransactionAuthenticated());
            }
            copyProfile(user, copy);
        }
        return copy;
    }

    private void copyProfile(UserAuthentication source, UserAuthentication target) {
        if (source == null || target == null || source.getProfile() == null || target.getProfile() == null) {
            return;
        }
        if (source.getProfile().getPersonUsername() != null && source.getProfile().getPersonId() != null) {
            target.getProfile().loadPersonInfo(source.getProfile().getPersonUsername(), source.getProfile().getPersonId());
        }
        if (source.getProfile().getMemberships() != null) {
            target.getProfile().loadMembership(source.getProfile().getMemberships());
        }
        target.getProfile().setServiceAccesses(source.getProfile().getServiceAccesses());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
