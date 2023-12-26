package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class AbstractFirstLvlStaticAccessAuthenticationProvider extends AbstractStaticAuthenticationProvider {
    protected AbstractFirstLvlStaticAccessAuthenticationProvider(CustomMD5Encoder encoder) {
        super(encoder);
    }
}
