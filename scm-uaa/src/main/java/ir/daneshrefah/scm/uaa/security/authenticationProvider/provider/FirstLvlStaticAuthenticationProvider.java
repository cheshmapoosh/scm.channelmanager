package ir.daneshrefah.scm.uaa.security.authenticationProvider.provider;

import ir.daneshrefah.scm.uaa.security.token.FirstLvlStaticAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    public FirstLvlStaticAuthenticationProvider(PasswordEncoder encoder) {
        super(encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlStaticAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
