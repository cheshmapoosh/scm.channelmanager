package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

import java.time.Instant;

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

    private final UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();

    private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(GeneralAuthenticationToken.class, authentication,
                () -> "AbstractAuthenticationProvider.onlySupports GeneralAuthenticationToken.");

        TerminalUserDetails user = (TerminalUserDetails) authentication.getPrincipal();
        try {
            this.preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user, (GeneralAuthenticationToken) authentication);
        } catch (AuthenticationException ex) {
            logger.warn("exception on authenticate: " + safeMessage(ex));
            return createFailAuthentication(authentication, ex);
        }
        this.postAuthenticationChecks.check(user);
        return createSuccessAuthentication((GeneralAuthenticationToken) authentication);
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

    protected Authentication createSuccessAuthentication(GeneralAuthenticationToken authentication) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        PostAuthenticationToken result = PostAuthenticationToken.authenticated(
                authentication.getPrincipal(),
                authentication.getDetails(),
                authentication.getPrincipal().getAuthorities());
        result.setSessionRequired(authentication.isSessionRequired());
        result.setNotificationRequired(authentication.isNotificationRequired());
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(authentication.getDetails()
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

    private String safeMessage(Exception exception) {
        if (exception == null || exception.getMessage() == null) {
            return exception == null ? null : exception.getClass().getSimpleName();
        }
        String message = exception.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replaceAll("(?i)(password|token|authorization|client_secret|authorization_code|pin|otp|session[_-]?id|cookie|mobile|national[_-]?code)\\s*[:=]\\s*\\S+", "$1=***")
                .trim();
        return message.length() > 300 ? message.substring(0, 300) : message;
    }

}
