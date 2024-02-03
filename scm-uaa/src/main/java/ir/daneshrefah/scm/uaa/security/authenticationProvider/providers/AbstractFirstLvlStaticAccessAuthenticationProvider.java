package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.service.UserService;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-23
 */
public abstract class AbstractFirstLvlStaticAccessAuthenticationProvider extends AbstractStaticAuthenticationProvider {
    protected AbstractFirstLvlStaticAccessAuthenticationProvider(UserService userService, CustomMD5Encoder encoder) {
        super(userService, encoder);
    }

    @Override
    public String extractCurrentPassword(TerminalUserDetails userDetails) {
        return userDetails.getPassword();
    }
}
