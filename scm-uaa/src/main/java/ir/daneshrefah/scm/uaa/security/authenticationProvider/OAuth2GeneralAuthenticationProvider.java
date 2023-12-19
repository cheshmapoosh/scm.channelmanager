package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2ClientAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.stereotype.Component;

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
    //    private final OAuth2AuthorizationService authorizationService;
//    private final OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator;
    private final UserDetailsService userDetailsService;
    private final AuthenticationTokenGenerator authenticationTokenGenerator;
    private final DelegatorAuthenticationProvider delegatorAuthenticationProvider;

    public OAuth2GeneralAuthenticationProvider(/*OAuth2AuthorizationService authorizationService,
                                               OAuth2TokenGenerator<? extends OAuth2Token> tokenGenerator,*/
            UserDetailsService userDetailsService,
            AuthenticationTokenGenerator authenticationTokenGenerator,
            DelegatorAuthenticationProvider delegatorAuthenticationProvider) {
//        Assert.notNull(authorizationService, "authorizationService cannot be null");
//        Assert.notNull(tokenGenerator, "tokenGenerator cannot be null");
//        this.authorizationService = authorizationService;
//        this.tokenGenerator = tokenGenerator;
        this.userDetailsService = userDetailsService;
        this.authenticationTokenGenerator = authenticationTokenGenerator;
        this.delegatorAuthenticationProvider = delegatorAuthenticationProvider;
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

        UserDetails userDetails = null;
        try {
            userDetails = userDetailsService.loadUserByUsername(preAuthenticationToken.getName(), terminalCode);
        } catch (UsernameNotFoundException e) {
            this.logger.warn("user not found for: " + preAuthenticationToken.getName() + ":" + terminalCode);
        }

        if (userDetails == null) {
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.INVALID_GRANT);
        }

        if (this.logger.isTraceEnabled()) {
            this.logger.trace("Retrieved authorization with authorization code");
        }

        AbstractAuthenticationToken token = authenticationTokenGenerator.generateToken(
                preAuthenticationToken, (TerminalUserDetails) userDetails);

        delegatorAuthenticationProvider.authenticate(token);

        return null;
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

    @Override
    public boolean supports(Class<?> authentication) {
        return PreAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
