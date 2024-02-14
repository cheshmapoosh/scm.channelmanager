package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlPatternAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlPatternAuthenticationProvider extends AbstractFirstLvlStaticAccessAuthenticationProvider {


    protected FirstLvlPatternAuthenticationProvider(UserService userService, CustomMD5Encoder encoder) {
        super(userService, encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlPatternAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
