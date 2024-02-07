package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_SESSION;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-06
 */
@Slf4j
@RequiredArgsConstructor
public abstract class BaseGeneralAuthenticationProvider implements AuthenticationProvider {

    protected final RegisteredClientRepository clientRepository;
    private final UserCache userCache;
    private final UserDetailsService userDetailsService;
    private final OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator;
    private final DelegatorAuthenticationProvider delegatorAuthenticationProvider;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticationToken preAuthenticationToken = extractPreAuthenticationToken(authentication);
        if (null == preAuthenticationToken.getRegisteredClient()) {
            preAuthenticationToken.setRegisteredClient(clientRepository.findByClientId(preAuthenticationToken.getClientId()));
        }
        String clientTerminalCode = preAuthenticationToken.getRegisteredClient().getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);

        boolean cacheWasUsed = true;
        String cacheUserKey = extractCacheUserKey(preAuthenticationToken, clientTerminalCode);
        UserDetails userDetails = this.userCache.getUserFromCache(cacheUserKey);
        if (userDetails == null) {
            cacheWasUsed = false;
            try {
                userDetails = retrieveUser(preAuthenticationToken.getName(), clientTerminalCode);
            } catch (UsernameNotFoundException ex) {
                log.debug("Failed to find user '" + preAuthenticationToken.getName() + "'");
                throw new BadCredentialsException("AbstractUserDetailsAuthenticationProvider.badCredentials");
            }
        }
        if (userDetails == null) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_USERNAME);
        }
        if (!cacheWasUsed) {
            this.userCache.putUserInCache(userDetails);
        }
        if (log.isTraceEnabled()) {
            log.trace("Retrieved userDetails with username: " + preAuthenticationToken.getName() + ":" + clientTerminalCode);
        }
        AbstractAuthenticationToken token = null;
        try {
            token = authenticationTokenGenerator.generateToken(
                    preAuthenticationToken, (TerminalUserDetails) userDetails);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }
//        TODO check session required
        token.setSessionRequired(preAuthenticationToken.getScopes().contains(OAUTH2_SCOPE_NAME_SESSION));
        token.setNotificationRequired(AuthorizationGrantType.AUTHORIZATION_CODE.equals(preAuthenticationToken.getGrantType()) ||
                AuthorizationGrantType.FIRST_PASSWORD.equals(preAuthenticationToken.getGrantType()));

        GeneralAuthenticationToken authorization = (GeneralAuthenticationToken) delegatorAuthenticationProvider.authenticate(token);
        if (authorization == null || !authorization.isAuthenticated()) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_PASSWORD);
        }
        if (log.isTraceEnabled()) {
            log.trace("authentication completed successfully.");
        }

        return buildResponse(authentication, preAuthenticationToken, authorization);
    }

    protected abstract Authentication buildResponse(Authentication requestAuthentication,
                                                    PreAuthenticationToken preAuthenticationToken,
                                                    GeneralAuthenticationToken authentication);

    private UserDetails retrieveUser(String username, String terminalCode)
            throws AuthenticationException {
        try {
            return userDetailsService.loadUserByUsername(username, terminalCode);
        } catch (UsernameNotFoundException e) {
            log.warn("user not found for: " + username + ":" + terminalCode);
        }
        return null;
    }

    protected abstract void throwError(String errorCode, String parameterName);

    private String extractCacheUserKey(PreAuthenticationToken authenticationToken, String terminalCode) {
        return authenticationToken.getName() + StringUtils.DOUBLE_COLON +
                terminalCode;
    }

    protected abstract PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication);

}
