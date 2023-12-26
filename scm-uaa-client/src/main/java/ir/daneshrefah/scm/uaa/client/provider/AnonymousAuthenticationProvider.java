package ir.daneshrefah.scm.uaa.client.provider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.uaa.client.converter.token.TokenConverter;
import ir.daneshrefah.scm.uaa.client.provider.token.BaseAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.provider.token.ClientAuthenticationToken;
import ir.daneshrefah.scm.uaa.client.remote.RemoteSecurityServiceProvider;
import ir.daneshrefah.scm.uaa.common.core.SessionCache;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-24
 */
@ConditionalOnProperty(name = "scm.security.distributed", havingValue = "true", matchIfMissing = false)
@Component
public class AnonymousAuthenticationProvider extends AbstractClientAuthenticationProvider {

    private final String key = "scm_anonymous";

    protected AnonymousAuthenticationProvider(SessionCache sessionCache) {
        super(sessionCache);
    }

    @Override
    public UserAuthentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            return null;
        }
        if (this.key.hashCode() != ((AnonymousAuthenticationToken) authentication).getKeyHash()) {
            throw new BadCredentialsException(this.messages.getMessage("AnonymousAuthenticationProvider.incorrectKey",
                    "The presented AnonymousAuthenticationToken does not contain the expected key"));
        }
        return new UserAuthentication(null, authentication.getAuthorities());
    }

    @Override
    protected UserAuthentication retrieveUser(String username, BaseAuthenticationToken authentication) throws AuthenticationException {
        return null;
    }

    @Override
    protected void additionalAuthenticationChecks(UserAuthentication userAuthentication, BaseAuthenticationToken authentication) throws AuthenticationException {

    }

    @Override
    public boolean supports(Class<?> authentication) {
        return AnonymousAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
