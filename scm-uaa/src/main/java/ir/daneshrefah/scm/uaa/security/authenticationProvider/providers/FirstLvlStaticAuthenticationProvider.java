package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlStaticAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.UserService;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-19
 */
@Component
public class FirstLvlStaticAuthenticationProvider extends AbstractFirstLvlStaticAccessAuthenticationProvider {


    protected FirstLvlStaticAuthenticationProvider(UserService userService, CustomMD5Encoder encoder) {
        super(userService, encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlStaticAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
