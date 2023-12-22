package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public abstract class AbstractClientAuthenticationProvider implements AuthenticationProvider {

    protected final CacheTemplate cacheTemplate;

    protected AbstractClientAuthenticationProvider(CacheTemplate cacheTemplate) {
        this.cacheTemplate = cacheTemplate;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return null;
    }

}
