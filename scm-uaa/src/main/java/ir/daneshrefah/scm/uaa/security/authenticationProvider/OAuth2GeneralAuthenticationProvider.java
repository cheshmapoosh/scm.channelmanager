package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.security.authenticationProvider.provider.AbstractAuthenticationProvider;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.cache.NullUserCache;
import org.springframework.security.oauth2.core.*;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.context.AuthorizationServerContextHolder;
import org.springframework.security.oauth2.server.authorization.token.DefaultOAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenGenerator;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;

import static ir.daneshrefah.scm.uaa.constants.UAAConstants.CLIENT_SETTING_KEY_TERMINAL_CODE;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-18
 */
@Component
public class OAuth2GeneralAuthenticationProvider implements AuthenticationProvider {

    private final Log logger = LogFactory.getLog(getClass());
    private final UserCache userCache;
    private final AuthenticationTokenGenerator authenticationTokenGenerator = new AuthenticationTokenGenerator();
    private final UserDetailsService userDetailsService;
    private final DelegatorAuthenticationProvider delegatorAuthenticationProvider;
    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;

    public OAuth2GeneralAuthenticationProvider(UserDetailsService userDetailsService,
                                               OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,
                                               List<AbstractAuthenticationProvider> providers,
                                               @Nullable UserCache userCache) {
        Assert.notNull(userDetailsService, "userDetailsService cannot be null");
        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
        this.userDetailsService = userDetailsService;
        this.delegatorAuthenticationProvider = new DelegatorAuthenticationProvider(providers);
        this.tokenGenerator = tokenGenerator;
        this.userCache = null != userCache ? userCache : new NullUserCache();
    }

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
        UserDetails userDetails = this.userCache.getUserFromCache(preAuthenticationToken.getName());
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
            logger.error(e);
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }

        GeneralAuthenticationToken authorization = (GeneralAuthenticationToken) delegatorAuthenticationProvider.authenticate(token);
        if (authorization == null || !authorization.isAuthenticated()) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }
        if (this.logger.isTraceEnabled()) {
            this.logger.trace("authentication completed successfully.");
        }

        DefaultOAuth2TokenContext.Builder tokenContextBuilder = DefaultOAuth2TokenContext.builder()
                .registeredClient(registeredClient)
                .principal(authorization)
                .authorizationServerContext(AuthorizationServerContextHolder.getContext())
//                .authorization(authorization)
//                .authorizedScopes(authorization.getAuthorizedScopes())
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrant(authentication);

        OAuth2TokenContext tokenContext = tokenContextBuilder.tokenType(OAuth2TokenType.ACCESS_TOKEN).build();
        OAuth2Token generatedAccessToken = this.tokenGenerator.generate(tokenContext);

        OAuth2AccessToken accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER,
                generatedAccessToken.getTokenValue(), generatedAccessToken.getIssuedAt(),
                generatedAccessToken.getExpiresAt(), tokenContext.getAuthorizedScopes());

        return new OAuth2AccessTokenAuthenticationToken(
                registeredClient, clientPrincipal, accessToken/*, refreshToken, additionalParameters*/);

    }

    private User getAuthenticatedUserChannelElseThrowInvalidUsr(String username, String terminalCode) throws UsernameNotFoundException {
        // Ensure the user is authenticated
        return null;
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
