package ir.daneshrefah.scm.uaa.security.form;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.oauth2.error.OAuth2AuthenticationErrorMapper;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
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
public abstract class UaaAuthenticationMethodProviderSupport implements AuthenticationProvider {
    protected final Log logger = LogFactory.getLog(getClass());
    private final OAuth2AuthenticationErrorMapper errorMapper;
    private final UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();
    private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(GeneralAuthenticationToken.class, authentication,
                () -> "UaaAuthenticationMethodProviderSupport only supports GeneralAuthenticationToken.");

        TerminalUserDetails user = (TerminalUserDetails) authentication.getPrincipal();
        try {
            preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user, (GeneralAuthenticationToken) authentication);
        } catch (AuthenticationException exception) {
            logger.warn("exception on authenticate: " + errorMapper.safeMessage(exception));
            return createFailAuthentication(authentication, exception);
        }
        postAuthenticationChecks.check(user);
        return createSuccessAuthentication((GeneralAuthenticationToken) authentication);
    }

    protected Authentication createFailAuthentication(Authentication authentication, Exception exception) {
        PostAuthenticationToken result = PostAuthenticationToken.unauthenticated(
                (TerminalUserDetails) authentication.getPrincipal(),
                ((GeneralAuthenticationToken) authentication).getDetails(),
                exception
        );
        logger.debug("Unauthenticated user");
        return result;
    }

    protected Authentication createSuccessAuthentication(GeneralAuthenticationToken authentication) {
        PostAuthenticationToken result = PostAuthenticationToken.authenticated(
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
            GeneralAuthenticationToken authentication
    ) throws AuthenticationException;

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {
        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                UaaAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException("User account is locked");
            }
            if (!user.isEnabled()) {
                UaaAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException("User is disabled");
            }
            if (!user.isAccountNonExpired()) {
                UaaAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException("User account has expired");
            }
        }
    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {
        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                UaaAuthenticationMethodProviderSupport.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException("User credentials have expired");
            }
        }
    }
}
