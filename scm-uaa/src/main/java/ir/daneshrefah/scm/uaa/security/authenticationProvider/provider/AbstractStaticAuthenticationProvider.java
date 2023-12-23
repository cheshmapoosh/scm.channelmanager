package ir.daneshrefah.scm.uaa.security.authenticationProvider.provider;

import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class AbstractStaticAuthenticationProvider extends AbstractAuthenticationProvider {

    protected final PasswordEncoder encoder;

    protected AbstractStaticAuthenticationProvider(PasswordEncoder encoder) {
        this.encoder = encoder;
    }
}
