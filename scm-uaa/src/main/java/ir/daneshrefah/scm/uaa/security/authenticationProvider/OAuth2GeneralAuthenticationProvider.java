package ir.daneshrefah.scm.uaa.security.authenticationProvider;


import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.core.endpoint.OAuth2ParameterNames;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_SESSION;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;


/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Component
@AllArgsConstructor
public class OAuth2GeneralAuthenticationProvider implements AuthenticationProvider {

    private final static Logger logger = LoggerFactory.getLogger(OAuth2GeneralAuthenticationProvider.class);
    private AuthenticationRequestTokenGenerator authenticationTokenGenerator;
    private final UserCache userCache;
    private final UserDetailsService userDetailsService;
    private final DelegatorAuthenticationProvider delegatorAuthenticationProvider;
    private final AuthenticationResponseTokenGenerator responseTokenGenerator;
    private final ClientService clientService;
    private final RegisteredClientRepository clientRepository;


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        Authentication clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(preAuthenticationToken);
        if (null != clientPrincipal && clientPrincipal instanceof OAuth2ClientAuthenticationToken) {
            preAuthenticationToken.setRegisteredClient(((OAuth2ClientAuthenticationToken) clientPrincipal).getRegisteredClient());
        } else {
            preAuthenticationToken.setRegisteredClient(clientRepository.findByClientId(preAuthenticationToken.getClientId()));
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved registered client");
        }

        checkClientRequirementsElseThrowInvalidClient(preAuthenticationToken);

        final String clientTerminalCode = preAuthenticationToken.getRegisteredClient()
                .getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);

        boolean cacheWasUsed = true;
        String cacheUserKey = extractCacheUserKey(preAuthenticationToken, clientTerminalCode);
        UserDetails userDetails = this.userCache.getUserFromCache(cacheUserKey);
        if (userDetails == null) {
            cacheWasUsed = false;
            try {
                userDetails = retrieveUser(preAuthenticationToken.getName(), clientTerminalCode);
            } catch (UsernameNotFoundException ex) {
                this.logger.debug("Failed to find user '" + preAuthenticationToken.getName() + "'");
                throw new BadCredentialsException("AbstractUserDetailsAuthenticationProvider.badCredentials");
            }
//            Assert.notNull(userDetails, "retrieveUser returned null - a violation of the interface contract");
        }

        if (userDetails == null) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }

        if (!cacheWasUsed) {
            this.userCache.putUserInCache(userDetails);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved userDetails with username: " + preAuthenticationToken.getName() + ":" + clientTerminalCode);
        }

        AbstractAuthenticationToken token = null;
        try {
            token = authenticationTokenGenerator.generateToken(
                    preAuthenticationToken, (TerminalUserDetails) userDetails);
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }

//        TODO check session required
        token.setSessionRequired(preAuthenticationToken.getScopes().contains(OAUTH2_SCOPE_NAME_SESSION));
        token.setNotificationRequired(true);

        GeneralAuthenticationToken authorization = (GeneralAuthenticationToken) delegatorAuthenticationProvider.authenticate(token);
        if (authorization == null || !authorization.isAuthenticated()) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("authentication completed successfully.");
        }

        return responseTokenGenerator.getAccessToken(authentication, clientPrincipal,
                preAuthenticationToken.getRegisteredClient(), authorization);

    }

    private String extractCacheUserKey(PreAuthenticationToken authenticationToken, String terminalCode) {
        return authenticationToken.getName() + StringUtils.DOUBLE_COLON +
                terminalCode;
    }

    private void checkClientRequirementsElseThrowInvalidClient(PreAuthenticationToken preAuthenticationToken) {
        RegisteredClient registeredClient = preAuthenticationToken.getRegisteredClient();
        if (null == registeredClient) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, OAuth2ParameterNames.CLIENT_ID);
        }
        Client client = clientService.findByClientId(registeredClient.getClientId());

        if (client.isRequireClientAuthentication()) {
            Authentication clientAuthentication = preAuthenticationToken.getClientPrincipal();
            if (null == clientAuthentication || !clientAuthentication.isAuthenticated() ||
                    clientAuthentication instanceof AnonymousAuthenticationToken) {
                throwError(OAuth2ErrorCodes.INVALID_CLIENT, Constants.OAUTH2_PARAM_NAME_CLIENT_AUTHENTICATION);
            }
        }

        if (!client.isCheckVersion()) {
            return;
        }
        Optional<ClientVersion> clientVersion = client.getVersions().stream()
                .filter(c -> c.getVersion().equals(preAuthenticationToken.getClientVersion()))
                .findFirst();

        if (clientVersion.isEmpty() || ClientVersionStatus.INVALID.equals(clientVersion.get().getStatus())) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, Constants.OAUTH2_PARAM_NAME_CLIENT_VERSION);
        }
        if (null != clientVersion.get().getSignature() &&
                !clientVersion.get().getSignature().equals(preAuthenticationToken.getClientSignature())) {
            throwError(OAuth2ErrorCodes.INVALID_CLIENT, Constants.OAUTH2_PARAM_NAME_CLIENT_SIGNATURE);
        }
    }

    private static Authentication getAuthenticatedClientElseThrowInvalidClient(PreAuthenticationToken authentication) {
        return authentication.getClientPrincipal();
//        OAuth2ClientAuthenticationToken clientPrincipal = null;
//        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getClientPrincipal().getClass())) {
//            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getClientPrincipal();
//        }
//        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
//            return clientPrincipal;
//        }
//        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
    }

    protected UserDetails retrieveUser(String username, String terminalCode)
            throws AuthenticationException {
        try {
            return userDetailsService.loadUserByUsername(username, terminalCode);
        } catch (UsernameNotFoundException e) {
            this.logger.warn("user not found for: " + username + ":" + terminalCode);
        }
        return null;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
