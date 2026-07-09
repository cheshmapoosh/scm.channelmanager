package ir.daneshrefah.scm.uaa.starter.provider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.starter.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.provider.token.ClaimAuthenticationToken;
import ir.daneshrefah.scm.uaa.starter.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.cache.CacheManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;

/**
 * Legacy remote claim provider for the inactive second-password flow.
 * Remove this class after legacy transaction authentication clients are migrated.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
@Deprecated(since = "9.0.0", forRemoval = true)
public class ClaimRemoteAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {

    public ClaimRemoteAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                             ObjectMapper objectMapper,
                                             SessionCache sessionCache,
                                             CacheManager cacheManager) {
        super(remoteSecurityServiceProvider, sessionCache, cacheManager);
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        throw new AuthenticationServiceException("second-password claim authentication is inactive");
    }

    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return ClaimAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
