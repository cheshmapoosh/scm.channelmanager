package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.service.LogoutService;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthenticationToken;

import java.util.HashMap;
import java.util.Map;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private static final String JWT_ID_CACHE_NAME = "jwt:jti";

    private final Log logger = LogFactory.getLog(getClass());

    private final JwtDecoder jwtDecoder;

    private final LogoutService logoutService;

    private final CacheManager cacheManager;

    private final UserService userService;

//    private final Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();
    private final Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtTokenConverter(/*() -> null*/);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        Jwt jwt = getJwt(bearer);
        jwt = prepareJwt(jwt, authentication);
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        validateJwtId(jwt,token);
        if (token.getDetails() == null) {
            token.setDetails(bearer.getDetails());
        }
        this.logger.debug("Authenticated token");
        return token;
    }

    private void validateJwtId(Jwt jwt, AbstractAuthenticationToken authentication) {
        String jwtTokenId = jwt.getClaim(Constants.CLAIM_KEY_JWT_IDENTIFIER);
        String username = jwt.getClaim("sub");
        String terminalCode = jwt.getClaim(Constants.CLAIM_KEY_TERMINAL);
        User user = userService.findByNicknameAndTerminalCode(username, terminalCode);
        if (user.getPerson().getPersonType().equals(PersonType.CLIENT)) {
            return;
        }
        String cacheKey = String.format("%s%s%s", username, "::", terminalCode);
        String cachedTokenId = cachedJwtId(cacheKey);
        if (StringUtils.isBlank(jwtTokenId) || !jwtTokenId.equals(cachedTokenId)) {
            logoutService.sendLogoutMessage(authentication);
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_TOKEN, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
    }

    private String cachedJwtId(String cacheKey) {
        Cache cache = cacheManager.getCache(JWT_ID_CACHE_NAME);
        if (cache == null) {
            throw new IllegalStateException("Spring cache is not configured: " + JWT_ID_CACHE_NAME);
        }
        Cache.ValueWrapper valueWrapper = cache.get(cacheKey);
        return valueWrapper == null ? null : (String) valueWrapper.get();
    }

    private Jwt prepareJwt(Jwt jwt, Authentication authentication) {
        Map<String, Object> headers = jwt.getHeaders();
        Map<String, Object> claims = new HashMap<>(jwt.getClaims());
        claims.put("claim_authentication", authentication);
        // @formatter:off
        return Jwt.withTokenValue(jwt.getTokenValue())
                .headers((h) -> h.putAll(headers))
                .claims((c) -> c.putAll(claims))
                .build();
    }

    private Jwt getJwt(BearerTokenAuthenticationToken bearer) {
        try {
            Jwt decodeJwt = this.jwtDecoder.decode(bearer.getToken());
            return decodeJwt;
        }
        catch (BadJwtException failed) {
            this.logger.debug("Failed to authenticate since the JWT was invalid");
            throw new InvalidBearerTokenException(failed.getMessage(), failed);
        }
        catch (JwtException failed) {
            throw new AuthenticationServiceException(failed.getMessage(), failed);
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
