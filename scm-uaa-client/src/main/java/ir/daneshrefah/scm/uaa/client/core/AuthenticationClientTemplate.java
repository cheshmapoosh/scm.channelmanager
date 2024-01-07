package ir.daneshrefah.scm.uaa.client.core;

import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.converter.authentication.*;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.utils.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationClientTemplate.class);

    private final AuthenticationManager authenticationManager;
    private final List<AuthenticationConverter> authenticationConverters;

    public AuthenticationClientTemplate(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
        this.authenticationConverters = Arrays.asList(new BasicAuthenticationConverter(), new BearerTokenResolver(),
                new SessionKeyResolver(), new ClientAuthenticationConverter(), new AnonymousAuthenticationConverter());
    }


    public UserAuthentication authenticateByAuthenticationRequest(ClientAuthenticationRequest request) {
        AbstractAuthenticationToken authToken = null;
        try {
            for (Iterator<AuthenticationConverter> iterator = authenticationConverters.iterator(); iterator.hasNext(); ) {
                AuthenticationConverter converter = iterator.next();
                authToken = converter.convertByRequest(request);
                if (null != authToken) {
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            throw new ClientAuthenticationException(e.getMessage(), null != e.getCause() ? e.getCause() : e,
                    generateFailAuthentication(authToken, e));
        }
        return authenticateByAuthenticationToken(authToken);
    }

    public UserAuthentication authenticateByAuthorizationHeader(String username, String terminalCode, String authorizationHeader) {
        AbstractAuthenticationToken authToken = null;
        try {
            for (Iterator<AuthenticationConverter> iterator = authenticationConverters.iterator(); iterator.hasNext(); ) {
                AuthenticationConverter converter = iterator.next();
                authToken = converter.convertByHeader(username, terminalCode, authorizationHeader);
                if (null != authToken) {
                    break;
                }
            }
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            throw new ClientAuthenticationException(e.getMessage(), null != e.getCause() ? e.getCause() : e,
                    generateFailAuthentication(authToken, e));
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
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            throw new ClientAuthenticationException(e.getMessage(), null != e.getCause() ? e.getCause() : e,
                    generateFailAuthentication(authToken, e));
        }
        return (UserAuthentication) authResult;
    }

    private UserAuthentication generateFailAuthentication(AbstractAuthenticationToken authToken, Exception e) {
        UserAuthentication.AuthenticationDetail detail = UserAuthentication.AuthenticationDetail.builder()
                .issuer(null)
                .issuedAt(null)
                .expiresAt(null)
                .maxIdle(null)
                .loginData(null)
                .loginAccessParameter(null)
                .sessionId(null)
                .clientId(null)
                .build();
        UserAuthentication result = new UserAuthentication(detail, null);
        result.setError(e.getMessage());
//        result.setException(ClassUtils.cloneExceptionWithoutStackTrace(e));
        return result;
    }

}
