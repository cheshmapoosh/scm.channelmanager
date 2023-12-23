package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class BasicAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {


    public BasicAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider) {
        super(remoteSecurityServiceProvider);
    }

    @Override
    protected UserDetails retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        boolean isAuthenticated = remoteSecurityServiceProvider.authenticateBasic((BasicAuthenticationToken) authentication);
        if (!isAuthenticated) {
            throw new InternalAuthenticationServiceException(
                    "remoteServiceProvider returned null, which is an interface contract violation");
        }
        UserDetails loadedUser = getUserCache().getUserFromCache(authentication.getId());
        if (loadedUser == null) {
            throw new InternalAuthenticationServiceException(
                    "userCache returned null, which is an interface contract violation");
        }
        return loadedUser;
    }

    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BasicAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
