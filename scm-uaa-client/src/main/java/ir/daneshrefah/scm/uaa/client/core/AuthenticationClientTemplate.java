package ir.daneshrefah.scm.uaa.client.core;

import ir.daneshrefah.scm.uaa.client.converter.*;
import ir.daneshrefah.scm.uaa.common.model.authentication.Authentication;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class AuthenticationClientTemplate {

    private final AuthenticationManager authenticationManager;
    private final List<AuthenticationConverter> authenticationConverters;

    public AuthenticationClientTemplate(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.authenticationConverters = Arrays.asList(new BasicAuthenticationConverter(), new BearerTokenResolver(),
                new SessionKeyResolver(), new ClientAuthenticationConverter());
    }


    public Authentication extractAuthenticationFromAuthorizationHeader(String terminalCode, String authorizationHeader) {
        AbstractAuthenticationToken authToken = null;
        for (Iterator<AuthenticationConverter> iterator = authenticationConverters.iterator(); iterator.hasNext(); ) {
            AuthenticationConverter converter = iterator.next();
            authToken = converter.convertByHeader(terminalCode, authorizationHeader);
            if (null != authToken) {
                break;
            }
        }

//        Authorization: Basic base64(username:password)
//        Authorization: Digest username="username", realm="realm", nonce="nonce", uri="uri", response="hash"
//        Authorization: Bearer token
//        Authorization: Session sessionKey

        org.springframework.security.core.Authentication authResult = this.authenticationManager.authenticate(authToken);
        return null;
    }

}
