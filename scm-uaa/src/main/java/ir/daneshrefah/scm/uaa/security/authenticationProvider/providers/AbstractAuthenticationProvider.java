package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.common.core.AuthorizationGrantType;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.util.Assert;

import java.time.Instant;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_CHECK_ACTIVATION;
import static ir.daneshrefah.scm.uaa.common.utils.Constants.CLIENT_SETTING_KEY_TERMINAL_CODE;
import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@RequiredArgsConstructor
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    private final UserService userService;

    private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();

    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(GeneralAuthenticationToken.class, authentication,
                () -> "AbstractAuthenticationProvider.onlySupports GeneralAuthenticationToken.");

        TerminalUserDetails user = (TerminalUserDetails) authentication.getPrincipal();
        try {
            this.preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user, (GeneralAuthenticationToken) authentication);
        } catch (AuthenticationException ex) {
            logger.warn("exception on authenticate", ex);
            return createFailAuthentication(authentication, ex);
        }
        this.postAuthenticationChecks.check(user);
        checkUserActivationCodeIfRequired((GeneralAuthenticationToken) authentication, user);
        return createSuccessAuthentication(authentication);
    }

    private void checkUserActivationCodeIfRequired(GeneralAuthenticationToken authentication, TerminalUserDetails user) {
        ClientSettings clientSettings = authentication.getDetails().getRegisteredClient().getClientSettings();
        boolean isClientSupportCheckActivation = clientSettings.getSetting(CLIENT_SETTING_KEY_CHECK_ACTIVATION);
        boolean isGrantTypeSupportCheckActivation = authentication.getDetails().getGrantType().isSupportActivationCheck();
        if (!isClientSupportCheckActivation || !isGrantTypeSupportCheckActivation) {
            return;
        }
        String terminalCode = clientSettings.getSetting(CLIENT_SETTING_KEY_TERMINAL_CODE);
        String username = authentication.getDetails().getName();
        String accessParameter = authentication.getDetails().getAccessParameter();
        String activationCode = authentication.getDetails().getActivationCode();
        boolean isActivated = userService.checkUserActivationCode(terminalCode, username, accessParameter, activationCode);
        if (!isActivated) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_USER, Constants.OAUTH2_PARAM_NAME_USER_ACTIVATION_CODE);
        }
    }

    protected Authentication createFailAuthentication(Authentication authentication, Exception exception) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        PostAuthenticationToken result = PostAuthenticationToken.unauthenticated(
                (TerminalUserDetails) authentication.getPrincipal(),
                ((GeneralAuthenticationToken) authentication).getDetails(), exception);
        this.logger.debug("Unauthenticated user");
        return result;
    }

    protected Authentication createSuccessAuthentication(Authentication authentication) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        GeneralAuthenticationToken authenticationToken = (GeneralAuthenticationToken) authentication;
        PostAuthenticationToken result = null;
        if (AuthorizationGrantType.SECOND_PASSWORD.equals(authenticationToken.getDetails().getGrantType())) {
            result = PostAuthenticationToken.secondLvlAuthenticated(
                    (TerminalUserDetails) authentication.getPrincipal(),
                    ((GeneralAuthenticationToken) authentication).getDetails());
        } else {
            result = PostAuthenticationToken.authenticated(
                    (TerminalUserDetails) authentication.getPrincipal(),
                    ((GeneralAuthenticationToken) authentication).getDetails(),
                    ((TerminalUserDetails) authentication.getPrincipal()).getAuthorities());
        }
        result.setSessionRequired(authenticationToken.isSessionRequired());
        result.setNotificationRequired(authenticationToken.isNotificationRequired());
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(authenticationToken.getDetails()
                .getRegisteredClient().getTokenSettings().getAccessTokenTimeToLive());
        result.setIssuedAt(issuedAt);
        result.setExpiresAt(expiresAt);
        this.logger.debug("Authenticated user");
        return result;
    }

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                AbstractAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException("AbstractAuthenticationProvider.locked: User account is locked");
            }
            if (!user.isEnabled()) {
                AbstractAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException("AbstractAuthenticationProvider.disabled: User is disabled");
            }
            if (!user.isAccountNonExpired()) {
                AbstractAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException("AbstractAuthenticationProvider.expired: User account has expired");
            }
        }

    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                AbstractAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException("AbstractAuthenticationProvider.credentialsExpired: " +
                        "User credentials have expired");
            }
        }

    }

    protected abstract void additionalAuthenticationChecks(TerminalUserDetails userDetails,
                                                           GeneralAuthenticationToken authentication) throws AuthenticationException;

}
