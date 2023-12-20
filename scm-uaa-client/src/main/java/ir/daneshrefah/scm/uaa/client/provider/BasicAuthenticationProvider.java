package ir.daneshrefah.scm.uaa.client.provider;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class BasicAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {


    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
