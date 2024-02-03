package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;


import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlSmsRequestAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.UserService;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlSmsRequestAuthenticationProvider extends AbstractStaticRequestAuthenticationProvider{


    protected FirstLvlSmsRequestAuthenticationProvider(UserService userService, CustomMD5Encoder encoder) {
        super(userService, encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlSmsRequestAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
