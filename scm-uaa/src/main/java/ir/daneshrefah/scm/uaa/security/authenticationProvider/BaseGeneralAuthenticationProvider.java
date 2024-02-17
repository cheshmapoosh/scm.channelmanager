package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.domain.client.Client;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.exception.*;
import ir.daneshrefah.scm.uaa.security.token.AbstractAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;
import java.util.Optional;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.*;

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
    protected final ClientService clientService;
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
        checkClientVersionIfRequired(preAuthenticationToken);
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
            throwError(preAuthenticationToken, null);
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

        GeneralAuthenticationToken authorization = null;
        try {
            authorization = (GeneralAuthenticationToken) delegatorAuthenticationProvider.authenticate(token);
        } catch (Exception e) {
            throwError(token, e);
        }
        Exception exception = authorization.getClass().isAssignableFrom(PostAuthenticationToken.class) ? ((PostAuthenticationToken) authorization).getException() : null;
        if (authorization == null || !authorization.isAuthenticated()) {
            throwError(token, exception);
        }
        if (log.isTraceEnabled()) {
            log.trace("authentication completed successfully.");
        }

        return buildResponse(authentication, preAuthenticationToken, authorization);
    }

    private void checkClientVersionIfRequired(PreAuthenticationToken preAuthenticationToken) {
        RegisteredClient registeredClient = preAuthenticationToken.getRegisteredClient();
        boolean isClientSupportCheckVersion = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_CHECK_VERSION);
        boolean isClientSupportCheckActivation = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_CHECK_ACTIVATION);
        if (isClientSupportCheckActivation && StringUtils.isEmpty(preAuthenticationToken.getActivationCode())) {
            throwError(preAuthenticationToken, new ActivationCodeRequiredException());
        }
        if (!isClientSupportCheckVersion)
            return;
        String userClientVersion = preAuthenticationToken.getClientVersion();
        if (StringUtils.isEmpty(userClientVersion)) {
            throwError(preAuthenticationToken, new ClientVersionRequiredException());
        }
        String userClientSignature = preAuthenticationToken.getClientSignature();
        List<ClientVersion> clientVersions = clientService.findByClientId(registeredClient.getClientId()).getVersions();
        Optional<ClientVersion> clientVersion = clientVersions.stream().filter(version -> userClientVersion.equals(version.getVersion())).findFirst();
        if (clientVersion.isEmpty()) {
            throwError(preAuthenticationToken, new InvalidClientVersionException(userClientVersion));
        }
        if (StringUtils.isNotEmpty(clientVersion.get().getSignature()) &&
                !clientVersion.get().getSignature().equals(userClientSignature)) {
            throwError(preAuthenticationToken, new InvalidClientSignatureException());
        }
    }

    protected String extractParameterName(Exception exception) {
        if (null == exception) {
            return Constants.OAUTH2_ERROR_CODE_INVALID_USER;
        }
        boolean isStepTwo = exception instanceof TwoStepAuthenticationRequiredException;
        exception = exception instanceof TwoStepAuthenticationRequiredException && null != exception.getCause() ?
                (Exception) exception.getCause() : exception;
        if (exception instanceof LockedException)
            return OAUTH2_ERROR_CODE_IS_LOCKED;
        else if (exception instanceof DisabledException)
            return OAUTH2_ERROR_CODE_IS_DISABLED;
        else if (exception instanceof AccountExpiredException)
            return OAUTH2_ERROR_CODE_IS_EXPIRED;
        else if (exception instanceof BadCredentialsException && isStepTwo)
            return OAUTH2_ERROR_CODE_INVALID_CLAIM;
        else if (exception instanceof BadCredentialsException && !isStepTwo)
            return OAUTH2_ERROR_CODE_INVALID_PASSWORD;
        else if (exception instanceof UsernameNotFoundException)
            return OAUTH2_ERROR_CODE_INVALID_USER;
        else if (exception instanceof TwoStepAuthenticationRequiredException)
            return OAUTH2_ERROR_CODE_REQUIRED_CLAIM;
        else if (exception instanceof BaseAuthenticationException)
            return ((BaseAuthenticationException) exception).getErrorCode();
        return exception.getMessage();
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

    protected abstract void throwError(Authentication authentication, Exception exception) throws AuthenticationException;

    private String extractCacheUserKey(PreAuthenticationToken authenticationToken, String terminalCode) {
        return authenticationToken.getName() + StringUtils.DOUBLE_COLON +
                terminalCode;
    }

    protected abstract PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication);

}
