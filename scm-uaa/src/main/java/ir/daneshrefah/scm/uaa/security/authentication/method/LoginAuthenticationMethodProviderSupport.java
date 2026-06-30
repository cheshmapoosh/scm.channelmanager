package ir.daneshrefah.scm.uaa.security.authentication.method;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.oauth2.error.OAuth2AuthenticationErrorMapper;
import ir.daneshrefah.scm.uaa.security.authentication.token.UserLoginAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.authentication.token.AuthenticationOutcomeToken;
import lombok.RequiredArgsConstructor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

import java.time.Instant;

@RequiredArgsConstructor
public abstract class LoginAuthenticationMethodProviderSupport implements AuthenticationProvider {
    protected final Log logger = LogFactory.getLog(getClass());
    private final OAuth2AuthenticationErrorMapper errorMapper;
    private final UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
    private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(UserLoginAuthenticationToken.class, authentication,
                () -> "LoginAuthenticationMethodProviderSupport only supports UserLoginAuthenticationToken.");

        TerminalUserDetails user = (TerminalUserDetails) authentication.getPrincipal();
        try {
            preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user, (UserLoginAuthenticationToken) authentication);
        } catch (AuthenticationException exception) {
            logger.warn("exception on authenticate: " + errorMapper.safeMessage(exception));
            return createFailAuthentication(authentication, exception);
        }
        postAuthenticationChecks.check(user);
        return createSuccessAuthentication((UserLoginAuthenticationToken) authentication);
    }

    protected Authentication createFailAuthentication(Authentication authentication, Exception exception) {
        AuthenticationOutcomeToken result = AuthenticationOutcomeToken.unauthenticated(
                (TerminalUserDetails) authentication.getPrincipal(),
                ((UserLoginAuthenticationToken) authentication).getDetails(),
                exception
        );
        logger.debug("Unauthenticated user");
        return result;
    }

    protected Authentication createSuccessAuthentication(UserLoginAuthenticationToken authentication) {
        AuthenticationOutcomeToken result = AuthenticationOutcomeToken.authenticated(
                authentication.getPrincipal(),
                authentication.getDetails(),
                authentication.getPrincipal().getAuthorities()
        );
        result.setSessionRequired(authentication.isSessionRequired());
        result.setNotificationRequired(authentication.isNotificationRequired());
        Instant issuedAt = Instant.now();
        Instant expiresAt = issuedAt.plus(authentication.getDetails()
                .getRegisteredClient()
                .getTokenSettings()
                .getAccessTokenTimeToLive());
        result.setIssuedAt(issuedAt);
        result.setExpiresAt(expiresAt);
        logger.debug("Authenticated user");
        return result;
    }

    protected abstract void additionalAuthenticationChecks(
            TerminalUserDetails userDetails,
            UserLoginAuthenticationToken authentication
    ) throws AuthenticationException;

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                LoginAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException("User account is locked");
            }
            if (!user.isEnabled()) {
                LoginAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException("User is disabled");
            }
            if (!user.isAccountNonExpired()) {
                LoginAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException("User account has expired");
            }
        }
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                LoginAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException("User credentials have expired");
            }
        }
    }
}
