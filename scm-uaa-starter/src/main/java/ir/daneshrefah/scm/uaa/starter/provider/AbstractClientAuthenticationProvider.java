package ir.daneshrefah.scm.uaa.starter.provider;

import ir.daneshrefah.scm.uaa.starter.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cache.CacheManager;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;
import org.springframework.security.core.authority.mapping.NullAuthoritiesMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.util.Assert;

import static ir.daneshrefah.scm.common.constant.SecurityConstants.ROLE_CSP;
import static ir.daneshrefah.scm.common.constant.SecurityConstants.USERNAME_NONE_PROVIDED;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public abstract class AbstractClientAuthenticationProvider implements AuthenticationProvider {

    protected final Log logger = LogFactory.getLog(getClass());

    private final SessionCache sessionCache;

    private final UserDetailsChecker preAuthenticationChecks = new DefaultPreAuthenticationChecks();

    private final UserDetailsChecker postAuthenticationChecks = new DefaultPostAuthenticationChecks();

    private final boolean forcePrincipalAsString = false;

    protected boolean hideUserNotFoundExceptions = true;

    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();

    private final GrantedAuthoritiesMapper authoritiesMapper = new NullAuthoritiesMapper();

    private final CacheManager cacheManager;

    protected AbstractClientAuthenticationProvider(SessionCache sessionCache, CacheManager cacheManager) {
        this.sessionCache = sessionCache;
        this.cacheManager = cacheManager;
    }

    @Override
    public UserAuthentication authenticate(Authentication authentication) throws AuthenticationException {
        Assert.isInstanceOf(BaseAuthenticationToken.class, authentication,
                () -> this.messages.getMessage("AbstractClientAuthenticationProvider.onlySupports",
                        "Only BaseAuthenticationToken is supported"));
        String username = determineUsername(authentication);
        boolean cacheWasUsed = true;
        String sessionKey = ((BaseAuthenticationToken) authentication).getSessionCacheKey();
        UserAuthentication user = null;
        if (StringUtils.isNotEmpty(sessionKey)) {
            user = this.sessionCache.getSessionFromCache(sessionKey);
        }
        if (user == null) {
            cacheWasUsed = false;
            try {
                user = retrieveUser(username, (BaseAuthenticationToken) authentication);
            }
            catch (UsernameNotFoundException ex) {
                this.logger.debug("Failed to find user '" + safeIdentifier(username) + "'");
                if (!this.hideUserNotFoundExceptions) {
                    throw ex;
                }
                throw new BadCredentialsException(this.messages
                        .getMessage("AbstractClientAuthenticationProvider.badCredentials", "Bad credentials"));
            }
            Assert.notNull(user, "retrieveUser returned null - a violation of the interface contract");
        }
        if (!USERNAME_NONE_PROVIDED.equals(username) && StringUtils.notEqualsIgnoreCase(username, user.getPrincipal().getNickname()) &&
                !user.hasAuthority(ROLE_CSP)) {
            throw new UsernameNotFoundException("not match username");
        }
        try {
            additionalAuthenticationChecks(user, (BaseAuthenticationToken) authentication);
        }
        catch (AuthenticationException ex) {
            if (!cacheWasUsed) {
                throw ex;
            }
            // There was a problem, so try again after checking
            // we're using latest data (i.e. not from the cache)
            cacheWasUsed = false;
            user = retrieveUser(username, (BaseAuthenticationToken) authentication);
            additionalAuthenticationChecks(user, (BaseAuthenticationToken) authentication);
        }
        if (!cacheWasUsed && StringUtils.isNotEmpty(sessionKey)) {
            this.sessionCache.putSessionInCache(sessionKey, user);
        }
        Object principalToReturn = user;
        if (this.forcePrincipalAsString) {
            principalToReturn = user.getName();
        }
        return createSuccessAuthentication(user, authentication);
    }

    private String determineUsername(Authentication authentication) {
        return (authentication.getPrincipal() == null) ? USERNAME_NONE_PROVIDED : authentication.getName();
    }

    private String safeIdentifier(String value) {
        if (StringUtils.isBlank(value) || USERNAME_NONE_PROVIDED.equals(value)) {
            return value;
        }
        String text = value.trim();
        if (text.length() <= 4) {
            return "****";
        }
        return text.substring(0, 2) + "***" + text.substring(text.length() - 2);
    }

    protected abstract UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication)
            throws AuthenticationException;

    protected abstract void additionalAuthenticationChecks(UserAuthentication userAuthentication,
                                                           BaseAuthenticationToken authentication) throws AuthenticationException;

    private class DefaultPreAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isAccountNonLocked()) {
                AbstractClientAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is locked");
                throw new LockedException(AbstractClientAuthenticationProvider.this.messages
                        .getMessage("AbstractClientAuthenticationProvider.locked", "User account is locked"));
            }
            if (!user.isEnabled()) {
                AbstractClientAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account is disabled");
                throw new DisabledException(AbstractClientAuthenticationProvider.this.messages
                        .getMessage("AbstractClientAuthenticationProvider.disabled", "User is disabled"));
            }
            if (!user.isAccountNonExpired()) {
                AbstractClientAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account has expired");
                throw new AccountExpiredException(AbstractClientAuthenticationProvider.this.messages
                        .getMessage("AbstractClientAuthenticationProvider.expired", "User account has expired"));
            }
        }

    }

    private class DefaultPostAuthenticationChecks implements UserDetailsChecker {

        @Override
        public void check(UserDetails user) {
            if (!user.isCredentialsNonExpired()) {
                AbstractClientAuthenticationProvider.this.logger
                        .debug("Failed to authenticate since user account credentials have expired");
                throw new CredentialsExpiredException(AbstractClientAuthenticationProvider.this.messages
                        .getMessage("AbstractClientAuthenticationProvider.credentialsExpired",
                                "User credentials have expired"));
            }
        }

    }

    protected UserAuthentication createSuccessAuthentication(UserAuthentication user, Authentication authentication) {
        return user;
    }

    protected SessionCache getSessionCache() {
        return sessionCache;
    }

    protected CacheManager cacheManager() {
        return cacheManager;
    }

}
