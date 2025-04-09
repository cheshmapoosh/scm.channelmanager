package ir.daneshrefah.scm.uaa.security.authenticationProvider;

import ir.daneshrefah.scm.common.constant.TerminalCodes;
import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersion;
import ir.daneshrefah.scm.uaa.exception.*;
import ir.daneshrefah.scm.uaa.exception.activation.InvalidActivationTerminalCodeException;
import ir.daneshrefah.scm.uaa.exception.activation.UserActivatedBeforeException;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.generator.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.activation.UserActivationAuthenticationService;
import ir.daneshrefah.scm.uaa.service.client.ClientService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

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
    private final UserActivationAuthenticationService userActivationAuthenticationService;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticationToken preAuthenticationToken = extractPreAuthenticationToken(authentication);
        if (null == preAuthenticationToken.getRegisteredClient()) {
            preAuthenticationToken.setRegisteredClient(clientRepository.findByClientId(preAuthenticationToken.getClientId()));
        }
        checkClientAuthenticatedIfRequired(preAuthenticationToken);
        checkClientIpAddressMatchIfRequired(preAuthenticationToken);
        checkClientVersionIfRequired(preAuthenticationToken);
        String clientTerminalCode = preAuthenticationToken.getRegisteredClient().getClientSettings().getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);
        UserDetails userDetails = null;
        UserActivationAuthenticationService.CandidateStatus candidateStatus = userActivationAuthenticationService.checkActivationCandidate(preAuthenticationToken);
/*        if (candidateStatus.equals(UserActivationAuthenticationService.CandidateStatus.ACCEPTED)) {
            UserActivationAuthenticationService.AuthenticationStatus authenticationStatus = userActivationAuthenticationService
                    .checkAuthentication(authentication.getName(),
                            TerminalCodes.fromString(preAuthenticationToken.getActivatorTerminal()).orElse(null),
                            TerminalCodes.NIB);
            switch (authenticationStatus) {
                case USER_NOT_FOUND ->
                        throwError(authentication, new UsernameNotFoundException("Invalid username or password"));
                case ACTIVATED_BEFORE -> throwError(authentication, new UserActivatedBeforeException());
                default -> userDetails = getUserDetails(preAuthenticationToken, preAuthenticationToken.getName(), preAuthenticationToken.getActivatorTerminal(), false);
            }

        } else if (candidateStatus.equals(UserActivationAuthenticationService.CandidateStatus.HAS_ERROR)) {
            throwError(authentication, new InvalidActivationTerminalCodeException());
        } else {
            userDetails = getUserDetails(preAuthenticationToken, preAuthenticationToken.getName(), clientTerminalCode, true);
        }*/
        userDetails = getUserDetails(preAuthenticationToken, preAuthenticationToken.getName(), clientTerminalCode, true);
        GeneralAuthenticationToken token;
        try {
            token = authenticationTokenGenerator.generateToken(
                    preAuthenticationToken, (TerminalUserDetails) userDetails);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }

        token.setSessionRequired(preAuthenticationToken.getScopes().contains(OAUTH2_SCOPE_NAME_SESSION));
        token.setNotificationRequired(preAuthenticationToken.getGrantType().isSupportNotification());

        GeneralAuthenticationToken authorization = null;
        try {
            authorization = (GeneralAuthenticationToken) delegatorAuthenticationProvider.authenticate(token);
        } catch (Exception e) {
            log.error("authenticate is failed {}", token, e);
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

    private UserDetails getUserDetails(Authentication authentication, String username, String clientTerminalCode, boolean cacheable) {
        boolean cacheWasUsed = true;
        String cacheUserKey = extractCacheUserKey(username, clientTerminalCode);
        UserDetails userDetails = this.userCache.getUserFromCache(cacheUserKey);
        if (userDetails == null) {
            cacheWasUsed = false;
            try {
                userDetails = retrieveUser(username, clientTerminalCode);
            } catch (UsernameNotFoundException ex) {
                log.debug("Failed to find user '{}'", username);
                throw new BadCredentialsException("AbstractUserDetailsAuthenticationProvider.badCredentials");
            }
        }
        if (userDetails == null) {
            throwError(authentication, new UsernameNotFoundException("Failed to find user '" + username + "'"));
        }
        if (!cacheWasUsed && cacheable) {
            this.userCache.putUserInCache(userDetails);
        }
        if (log.isTraceEnabled()) {
            log.trace("Retrieved userDetails with username: {}:{}", username, clientTerminalCode);
        }
        return userDetails;
    }

    private void checkClientIpAddressMatchIfRequired(PreAuthenticationToken preAuthenticationToken) {
        RegisteredClient registeredClient = preAuthenticationToken.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throwError(preAuthenticationToken, new ClientIpAddressNotAllowedException("registeredClient is null"));
        }
        boolean checkIpAddress = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_CHECK_IP_ADDRESS);
        if (!checkIpAddress) {
            return;
        }

        Set<String> allowIpAddresses = registeredClient.getClientSettings().getSetting(CLIENT_SETTING_KEY_ALLOW_IP_ADDRESSES);
        if (CollectionUtils.isEmpty(allowIpAddresses)) {
            throwError(preAuthenticationToken, new ClientIpAddressNotAllowedException("allowIpAddresses is empty"));
        }

        boolean match = allowIpAddresses.stream().anyMatch(ipAddress -> AuthenticationUtils.isIpAddressMatches(ipAddress, preAuthenticationToken.getRemoteAddress()));
        if (!match) {
            throwError(preAuthenticationToken, new ClientIpAddressNotAllowedException());
        }
    }

    private void checkClientAuthenticatedIfRequired(PreAuthenticationToken preAuthenticationToken) {
        RegisteredClient registeredClient = preAuthenticationToken.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throwError(preAuthenticationToken, new ClientAuthenticationRequiredException());
        }
        if (!registeredClient.getClientAuthenticationMethods().contains(ClientAuthenticationMethod.NONE)) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (!AuthenticationUtils.isFullyAuthenticated()) {
                throwError(preAuthenticationToken, new ClientAuthenticationRequiredException());
            }
        }
    }

    private void checkClientVersionIfRequired(PreAuthenticationToken preAuthenticationToken) {
        RegisteredClient registeredClient = preAuthenticationToken.getRegisteredClient();
        if (Objects.isNull(registeredClient)) {
            throwError(preAuthenticationToken, new ClientCodeRequiredException());
        }
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
        List<ClientVersion> clientVersions = clientService.findByNickname(registeredClient.getClientId()).orElseThrow().getVersions();
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
        String message = exception.getMessage();
        if (StringUtils.isEmpty(message) && null != exception.getCause()) {
            message = exception.getCause().getMessage();
        }
        if (StringUtils.isEmpty(message)) {
            message = exception.getClass().getSimpleName();
        }
        return message;
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

    private String extractCacheUserKey(String username, String terminalCode) {
        return username + StringUtils.DOUBLE_COLON + terminalCode;
    }

    protected abstract PreAuthenticationToken extractPreAuthenticationToken(Authentication authentication);

}
