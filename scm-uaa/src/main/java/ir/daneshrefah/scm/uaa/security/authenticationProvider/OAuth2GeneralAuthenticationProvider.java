package ir.daneshrefah.scm.uaa.security.authenticationProvider;


import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.token.generator.AuthenticationResponseTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import static ir.daneshrefah.scm.uaa.common.model.UaaConstants.CLIENT_SETTING_KEY_TERMINAL_CODE;


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


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticationToken preAuthenticationToken = (PreAuthenticationToken) authentication;
        OAuth2ClientAuthenticationToken clientPrincipal =
                getAuthenticatedClientElseThrowInvalidClient(preAuthenticationToken);
        RegisteredClient registeredClient = clientPrincipal.getRegisteredClient();

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved registered client");
        }

        final String terminalCode = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);

        boolean cacheWasUsed = true;
        String cacheUserKey = extractCacheUserKey(preAuthenticationToken, terminalCode);
        UserDetails userDetails = this.userCache.getUserFromCache(cacheUserKey);
        if (userDetails == null) {
            cacheWasUsed = false;
            try {
                userDetails = retrieveUser(preAuthenticationToken.getName(), terminalCode);
            } catch (UsernameNotFoundException ex) {
                this.logger.debug("Failed to find user '" + preAuthenticationToken.getName() + "'");
                throw new BadCredentialsException("AbstractUserDetailsAuthenticationProvider.badCredentials");
            }
            Assert.notNull(userDetails, "retrieveUser returned null - a violation of the interface contract");
        }

        if (userDetails == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }

        if (!cacheWasUsed) {
            this.userCache.putUserInCache(userDetails);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved userDetails with username: " + preAuthenticationToken.getName() + ":" + terminalCode);
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
        token.setSessionRequired(true);
        token.setNotificationRequired(true);

        GeneralAuthenticationToken authorization = (GeneralAuthenticationToken) delegatorAuthenticationProvider.authenticate(token);
        if (authorization == null || !authorization.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("authentication completed successfully.");
        }

        return responseTokenGenerator.getAccessToken(authentication, clientPrincipal, registeredClient, authorization);

    }
    private String extractCacheUserKey(PreAuthenticationToken authenticationToken, String terminalCode) {
        return authenticationToken.getName() + StringUtils.DOUBLE_COLON +
                terminalCode;
    }



    private static OAuth2ClientAuthenticationToken getAuthenticatedClientElseThrowInvalidClient(PreAuthenticationToken authentication) {
        OAuth2ClientAuthenticationToken clientPrincipal = null;
        if (OAuth2ClientAuthenticationToken.class.isAssignableFrom(authentication.getClientPrincipal().getClass())) {
            clientPrincipal = (OAuth2ClientAuthenticationToken) authentication.getClientPrincipal();
        }
        if (clientPrincipal != null && clientPrincipal.isAuthenticated()) {
            return clientPrincipal;
        }
        throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_CLIENT);
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
