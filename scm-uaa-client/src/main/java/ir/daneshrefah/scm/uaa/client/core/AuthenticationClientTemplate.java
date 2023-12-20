package ir.daneshrefah.scm.uaa.client.core;

import ir.daneshrefah.scm.uaa.client.converter.BasicAuthenticationConverter;
import ir.daneshrefah.scm.uaa.client.converter.BearerTokenResolver;
import ir.daneshrefah.scm.uaa.client.converter.SessionKeyResolver;
import ir.daneshrefah.scm.uaa.common.model.authentication.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class AuthenticationClientTemplate {

    private BasicAuthenticationConverter authenticationConverterBasic = new BasicAuthenticationConverter();
    private BearerTokenResolver bearerTokenResolver = new BearerTokenResolver();
    private SessionKeyResolver sessionKeyResolver = new SessionKeyResolver();

    private final AuthenticationManager authenticationManager;

    public AuthenticationClientTemplate(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }


    public Authentication extractAuthenticationFromAuthorizationHeader(String authorizationHeader) {
        AbstractAuthenticationToken authToken = null;
        authToken = authenticationConverterBasic.convert(authorizationHeader);
        if (null == authToken) {
            authToken = bearerTokenResolver.resolve(authorizationHeader);
        }
        if (null == authToken) {
            authToken = sessionKeyResolver.resolve(authorizationHeader);
        }

//        Authorization: Basic base64(username:password)
//        Authorization: Digest username="username", realm="realm", nonce="nonce", uri="uri", response="hash"
//        Authorization: Bearer token
//        Authorization: Session sessionKey

        org.springframework.security.core.Authentication authResult = this.authenticationManager.authenticate(authToken);
        return null;
    }

}
