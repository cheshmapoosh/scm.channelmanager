package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import com.nimbusds.jwt.JWTClaimsSet;
import ir.daneshrefah.scm.uaa.common.token.JwtTokenConverter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.convert.converter.Converter;
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
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-05-19
 */
@RequiredArgsConstructor
public class JwtAuthenticationProvider implements AuthenticationProvider {

    private final Log logger = LogFactory.getLog(getClass());

    private final JwtDecoder jwtDecoder;

//    private final Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();
    private final Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtTokenConverter(() -> null);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        Jwt jwt = getJwt(bearer);
        jwt = prepareJwt(jwt, authentication);
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        if (token.getDetails() == null) {
            token.setDetails(bearer.getDetails());
        }
        this.logger.debug("Authenticated token");
        return token;
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
            return this.jwtDecoder.decode(bearer.getToken());
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
