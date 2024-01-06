package ir.daneshrefah.scm.uaa.client.core;

import ir.daneshrefah.scm.uaa.client.converter.authentication.*;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;

import java.util.Arrays;
import java.util.Collections;
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
                new SessionKeyResolver(), new ClientAuthenticationConverter(), new AnonymousAuthenticationConverter());
    }


    public UserAuthentication authenticateByAuthenticationRequest(ClientAuthenticationRequest request) {
        AbstractAuthenticationToken authToken = null;
        for (Iterator<AuthenticationConverter> iterator = authenticationConverters.iterator(); iterator.hasNext(); ) {
            AuthenticationConverter converter = iterator.next();
            authToken = converter.convertByRequest(request);
            if (null != authToken) {
                break;
            }
        }

        return authenticateByAuthenticationToken(authToken);
    }

    public UserAuthentication authenticateByAuthorizationHeader(String username, String terminalCode, String authorizationHeader) {
        AbstractAuthenticationToken authToken = null;
        for (Iterator<AuthenticationConverter> iterator = authenticationConverters.iterator(); iterator.hasNext(); ) {
            AuthenticationConverter converter = iterator.next();
            authToken = converter.convertByHeader(username, terminalCode, authorizationHeader);
            if (null != authToken) {
                break;
            }
        }

        return authenticateByAuthenticationToken(authToken);
    }

    private UserAuthentication authenticateByAuthenticationToken(AbstractAuthenticationToken authToken) {
//        Authorization: Basic base64(username:password)
//        Authorization: Digest username="username", realm="realm", nonce="nonce", uri="uri", response="hash"
//        Authorization: Bearer token
//        Authorization: Session sessionKey

        org.springframework.security.core.Authentication authResult = null;
        try {
            authResult = this.authenticationManager.authenticate(authToken);
        } catch (AuthenticationException e) {
            return generateFailAuthentication(authToken, e);
        }
        return (UserAuthentication) authResult;
    }

    private UserAuthentication generateFailAuthentication(AbstractAuthenticationToken authToken, AuthenticationException e) {
        User user = new User();
        user.setNickName(authToken.getName());
//        user.setTerminalCode(authToken.);
        TerminalUserDetails userDetails = new TerminalUserDetails(user);
        UserAuthentication result = new UserAuthentication(userDetails, Collections.emptyList());
        result.setAuthenticated(false);
        result.setException(e);
        return result;
    }

}
