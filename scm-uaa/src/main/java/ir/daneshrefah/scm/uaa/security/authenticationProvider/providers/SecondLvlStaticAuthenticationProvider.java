package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.SecondLvlStaticAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.UserService;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-28
 */
@Component
public class SecondLvlStaticAuthenticationProvider extends AbstractStaticAuthenticationProvider {


    protected SecondLvlStaticAuthenticationProvider(UserService userService, CustomMD5Encoder encoder) {
        super(userService, encoder);
    }

    @Override
    public String extractCurrentPassword(TerminalUserDetails userDetails) {
        return userDetails.getUser().getTransactionStaticPassword();
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SecondLvlStaticAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
