package ir.daneshrefah.scm.uaa.client.provider;

import ir.daneshrefah.scm.uaa.client.provider.token.SessionAuthenticationToken;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
public class SessionAuthenticationProvider extends AbstractRemoteClientAuthenticationProvider {


    @Override
    public boolean supports(Class<?> authentication) {
        return SessionAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
