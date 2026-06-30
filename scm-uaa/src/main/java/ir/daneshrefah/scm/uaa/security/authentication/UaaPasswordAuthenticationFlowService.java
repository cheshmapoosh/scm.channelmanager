package ir.daneshrefah.scm.uaa.security.authentication;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.oauth2.error.OAuth2AuthenticationErrorMapper;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.ActivationPolicy;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.ClientAuthenticationPolicy;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.ClientIpPolicy;
import ir.daneshrefah.scm.uaa.security.oauth2.policy.ClientVersionPolicy;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.PreAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.oauth2.token.OAuth2AuthenticationRequestTokenGenerator;
import ir.daneshrefah.scm.uaa.security.userDetails.UserDetailsService;
import ir.daneshrefah.scm.uaa.service.activation.pwa.services.authentication.PwaAuthenticationService;
import ir.daneshrefah.scm.utils.string.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_SCOPE_NAME_SESSION;

@Service
@RequiredArgsConstructor
@Slf4j
public class UaaPasswordAuthenticationFlowService {
    private final RegisteredClientRepository clientRepository;
    private final UserCache userCache;
    private final UserDetailsService userDetailsService;
    private final OAuth2AuthenticationRequestTokenGenerator authenticationTokenGenerator;
    private final AuthenticationManager authenticationManager;
    private final PwaAuthenticationService pwaAuthenticationService;
    private final ClientAuthenticationPolicy clientAuthenticationPolicy;
    private final ClientIpPolicy clientIpPolicy;
    private final ClientVersionPolicy clientVersionPolicy;
    private final ActivationPolicy activationPolicy;
    private final OAuth2AuthenticationErrorMapper errorMapper;

    public AuthenticationResult authenticate(PreAuthenticationToken preAuthenticationToken) {
        if (preAuthenticationToken.getRegisteredClient() == null) {
            preAuthenticationToken.setRegisteredClient(clientRepository.findByClientId(preAuthenticationToken.getClientId()));
        }
        clientAuthenticationPolicy.validate(preAuthenticationToken);
        clientIpPolicy.validate(preAuthenticationToken);
        clientVersionPolicy.validate(preAuthenticationToken);

        String clientTerminalCode = preAuthenticationToken.getRegisteredClient()
                .getClientSettings()
                .getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);
        ActivationPolicy.ActivationDecision activationDecision =
                activationPolicy.decide(preAuthenticationToken, clientTerminalCode);
        UserDetails userDetails = getUserDetails(
                preAuthenticationToken,
                activationDecision.username(),
                activationDecision.terminalCode(),
                activationDecision.cacheable()
        );

        if (preAuthenticationToken.hasDefaultGrantPreAuthToken()) {
            pwaAuthenticationService.preAuthenticateCheck(preAuthenticationToken);
        }

        UserLoginAuthenticationToken authenticationToken = createAuthenticationToken(
                preAuthenticationToken,
                (TerminalUserDetails) userDetails
        );
        authenticationToken.setSessionRequired(preAuthenticationToken.getScopes().contains(OAUTH2_SCOPE_NAME_SESSION));
        authenticationToken.setNotificationRequired(preAuthenticationToken.getGrantType().isSupportNotification());

        UserLoginAuthenticationToken authenticatedToken = authenticateMethod(authenticationToken);
        return new AuthenticationResult(preAuthenticationToken, authenticatedToken);
    }

    private UserLoginAuthenticationToken createAuthenticationToken(
            PreAuthenticationToken preAuthenticationToken,
            TerminalUserDetails userDetails
    ) {
        UserLoginAuthenticationToken token;
        try {
            token = authenticationTokenGenerator.generateToken(preAuthenticationToken, userDetails);
        } catch (Exception exception) {
            log.error("failed to generate authentication token: {}", errorMapper.safeMessage(exception));
            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.SERVER_ERROR);
        }
        if (token == null) {
            throw new BadCredentialsException("unsupported authentication method");
        }
        return token;
    }

    private UserLoginAuthenticationToken authenticateMethod(UserLoginAuthenticationToken authenticationToken) {
        try {
            UserLoginAuthenticationToken authenticatedToken =
                    (UserLoginAuthenticationToken) authenticationManager.authenticate(authenticationToken);
            Exception exception = authenticatedToken instanceof AuthenticationOutcomeToken postAuthenticationToken
                    ? postAuthenticationToken.getException()
                    : null;
            if (authenticatedToken == null || !authenticatedToken.isAuthenticated()) {
                if (exception instanceof RuntimeException runtimeException) {
                    throw runtimeException;
                }
                throw new BadCredentialsException("authentication failed");
            }
            log.trace("authentication completed successfully");
            return authenticatedToken;
        } catch (RuntimeException exception) {
            log.error("authenticate failed for token type {}: {}",
                    authenticationToken.getClass().getSimpleName(),
                    errorMapper.safeMessage(exception));
            throw exception;
        } catch (Exception exception) {
            log.error("authenticate failed for token type {}: {}",
                    authenticationToken.getClass().getSimpleName(),
                    errorMapper.safeMessage(exception));
            throw new AuthenticationServiceException("authentication failed", exception);
        }
    }

    private UserDetails getUserDetails(
            PreAuthenticationToken authentication,
            String username,
            String terminalCode,
            boolean cacheable
    ) {
        boolean cacheWasUsed = true;
        String cacheUserKey = username + StringUtils.DOUBLE_COLON + terminalCode;
        UserDetails userDetails = userCache.getUserFromCache(cacheUserKey);
        if (userDetails == null) {
            cacheWasUsed = false;
            try {
                userDetails = userDetailsService.loadUserByUsername(username, terminalCode);
            } catch (UsernameNotFoundException exception) {
                log.warn("user not found for username={} terminal={}", safeIdentifier(username), terminalCode);
                throw new BadCredentialsException("AbstractUserDetailsAuthenticationProvider.badCredentials");
            }
        }
        if (userDetails == null) {
            throw new UsernameNotFoundException("Failed to find user");
        }
        if (!cacheWasUsed && cacheable) {
            userCache.putUserInCache(userDetails);
        }
        log.trace("retrieved user details username={} terminal={}", safeIdentifier(username), terminalCode);
        return userDetails;
    }

    private String safeIdentifier(String value) {
        if (StringUtils.isBlank(value)) {
            return null;
        }
        String text = value.trim();
        if (text.length() <= 4) {
            return "****";
        }
        return text.substring(0, 2) + "***" + text.substring(text.length() - 2);
    }

    public record AuthenticationResult(
            PreAuthenticationToken preAuthenticationToken,
            UserLoginAuthenticationToken authentication
    ) {
    }
}
