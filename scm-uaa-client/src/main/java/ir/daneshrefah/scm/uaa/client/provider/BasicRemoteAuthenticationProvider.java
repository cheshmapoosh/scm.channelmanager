package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.BasicAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
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
@ConditionalOnProperty(name = "scm.security.distributed", havingValue = "true", matchIfMissing = false)
@Component
public class BasicRemoteAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {


    public BasicRemoteAuthenticationProvider(RemoteSecurityServiceProvider remoteSecurityServiceProvider,
                                             SessionCache sessionCache) {
        super(remoteSecurityServiceProvider, sessionCache);
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        BasicAuthenticationToken authenticationResult = remoteSecurityServiceProvider.authenticateBasic((BasicAuthenticationToken) authentication);
        if (null == authenticationResult) {
            throw new InternalAuthenticationServiceException(
                    "remoteServiceProvider returned null, which is an interface contract violation");
        }
//        UserDetails loadedUser = getSessionCache().getSessionFromCache(authentication.getId());
//        if (loadedUser == null) {
//            throw new InternalAuthenticationServiceException(
//                    "userCache returned null, which is an interface contract violation");
//        }
        return null;
    }

    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return BasicAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
