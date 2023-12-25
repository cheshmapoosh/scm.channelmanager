package ir.daneshrefah.scm.uaa.security.authenticationProvider.provider;

import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.util.Assert;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public abstract class AbstractAuthenticationProvider implements AuthenticationProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    private UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();

    private UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(GeneralAuthenticationToken.class, authentication,
                () -> "AbstractAuthenticationProvider.onlySupports GeneralAuthenticationToken.");

        TerminalUserDetails user = (TerminalUserDetails) authentication.getDetails();
        try {
            this.preAuthenticationChecks.check(user);
            additionalAuthenticationChecks(user, (GeneralAuthenticationToken) authentication);
        } catch (AuthenticationException ex) {
            logger.warn("exception on authenticate", ex);
            return createFailAuthentication(authentication);
        }
        this.postAuthenticationChecks.check(user);
        return createSuccessAuthentication(authentication);
    }

    protected Authentication createFailAuthentication(Authentication authentication) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        PostAuthenticationToken result = PostAuthenticationToken.unauthenticated(
                (TerminalUserDetails) authentication.getDetails(),
                ((GeneralAuthenticationToken) authentication).getPreAuthenticationToken());
        result.setDetails(authentication.getDetails());
        this.logger.debug("Authenticated user");
        return result;
    }

    protected Authentication createSuccessAuthentication(Authentication authentication) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        PostAuthenticationToken result = PostAuthenticationToken.authenticated(
                (TerminalUserDetails) authentication.getDetails(),
                ((GeneralAuthenticationToken) authentication).getPreAuthenticationToken(),
                ((TerminalUserDetails) authentication.getDetails()).getAuthorities());
        result.setDetails(authentication.getDetails());
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
