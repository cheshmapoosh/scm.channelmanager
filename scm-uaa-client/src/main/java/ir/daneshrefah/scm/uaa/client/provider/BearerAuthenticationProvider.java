package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.converter.token.TokenConverter;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BearerAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
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
public class BearerAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {

//    private final JwtDecoder jwtDecoder;
//    private Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter = new JwtAuthenticationConverter();
    private final TokenConverter<String> jwtAuthenticationConverter;

    public BearerAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                        TokenConverter<String> jwtAuthenticationConverter,
                                        SessionCache sessionCache) {
        super(remoteSecurityServiceProvider, sessionCache);
        this.jwtAuthenticationConverter = jwtAuthenticationConverter;
    }

    /*@Override
    public AuthenticationHeader authenticate(Authentication authentication) throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        Jwt jwt = getJwt(bearer);
        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        if (token.getDetails() == null) {
            token.setDetails(bearer.getDetails());
        }
        return token;
    }*/

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        BearerAuthenticationToken bearer = (BearerAuthenticationToken) authentication;
        UserAuthentication userAuthentication = jwtAuthenticationConverter.convert(bearer.getToken());
//        AbstractAuthenticationToken token = this.jwtAuthenticationConverter.convert(jwt);
        return userAuthentication;
    }



    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
