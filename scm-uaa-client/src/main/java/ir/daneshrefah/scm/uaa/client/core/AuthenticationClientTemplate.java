package ir.daneshrefah.scm.uaa.client.core;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.client.ClientAuthenticationException;
import ir.daneshrefah.scm.uaa.client.converter.authentication.*;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseTerminalAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.SecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationClientTemplate.class);

    private final AuthenticationManager authenticationManager;
    private final SecurityServiceProvider securityServiceProvider;
    private final List<AuthenticationConverter> authenticationConverters;
    private final List<AuthenticationConverter> transactionConverters;

    public AuthenticationClientTemplate(AuthenticationManager authenticationManager, SecurityServiceProvider securityServiceProvider) {
        this.authenticationManager = authenticationManager;
        this.securityServiceProvider = securityServiceProvider;
        this.authenticationConverters = Arrays.asList(new BasicAuthenticationConverter(), new BearerTokenAuthenticationConverter(),
                new SessionKeyAuthenticationConverter(), new ClientAuthenticationConverter(), new AnonymousAuthenticationConverter());
        this.transactionConverters = Arrays.asList(new ClaimTokenAuthenticationConverter(), new AnonymousAuthenticationConverter());
    }


    public UserAuthentication authenticateUserByAuthenticationRequest(ClientAuthenticationRequest request) {
        try {
            BaseTerminalAuthenticationToken authenticationRequestToken = extractTokenByAuthenticationRequest(request,
                    authenticationConverters);
            return authenticateByAuthenticationToken(authenticationRequestToken);
        } catch (ClientAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            throw new ClientAuthenticationException(e.getMessage(), null != e.getCause() ? e.getCause() : e,
                    generateFailAuthentication(e));
        }
    }

    public UserAuthentication authenticateTransactionByAuthenticationRequest(ClientAuthenticationRequest request) {
        try {
            BaseTerminalAuthenticationToken authenticationRequestToken = extractTokenByAuthenticationRequest(request,
                    transactionConverters);
            return authenticateByAuthenticationToken(authenticationRequestToken);
        } catch (ClientAuthenticationException e) {
            throw e;
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            throw new ClientAuthenticationException(e.getMessage(), null != e.getCause() ? e.getCause() : e,
                    generateFailAuthentication(e));
        }
    }

    public GeneralPerson findGeneralPersonByUsernameAndTerminalCode(String username, String terminalCode) {
        return securityServiceProvider.findGeneralPersonByUsernameAndTerminalCode(username, terminalCode);
    }

    private BaseTerminalAuthenticationToken extractTokenByAuthenticationRequest(ClientAuthenticationRequest request,
                                                                                List<AuthenticationConverter> authenticationConverters) {
        BaseTerminalAuthenticationToken result = null;
        for (Iterator<AuthenticationConverter> iterator = authenticationConverters.iterator(); iterator.hasNext(); ) {
            AuthenticationConverter converter = iterator.next();
            result = converter.convertByRequest(request);
            if (null != result) {
                break;
            }
        }
        return result;
    }

    private UserAuthentication authenticateByAuthenticationToken(AbstractAuthenticationToken authToken) {
//        Authorization: Basic base64(username:password)
//        Authorization: Digest username="username", realm="realm", nonce="nonce", uri="uri", response="hash"
//        Authorization: Bearer token
//        Authorization: Session sessionKey
//        Authorization: Claim code

        org.springframework.security.core.Authentication authResult = null;
        try {
            authResult = this.authenticationManager.authenticate(authToken);
        } catch (Exception e) {
            LOGGER.error("error on authentication", e);
            throw new ClientAuthenticationException(e.getMessage(), null != e.getCause() ? e.getCause() : e,
                    generateFailAuthentication(e));
        }
        return (UserAuthentication) authResult;
    }

    private UserAuthentication generateFailAuthentication(Exception e) {
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
        result.setError(null != e.getCause() ? e.getCause().getMessage() : e.getMessage());
        return result;
    }

}
