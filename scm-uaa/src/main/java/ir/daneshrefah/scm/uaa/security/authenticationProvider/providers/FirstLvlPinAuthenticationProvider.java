package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;


import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlPinAuthenticationToken;
import org.springframework.stereotype.Component;

@Component

public class FirstLvlPinAuthenticationProvider extends AbstractFirstLvlStaticAccessAuthenticationProvider {


    protected FirstLvlPinAuthenticationProvider(CustomMD5Encoder encoder) {
        super(encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlPinAuthenticationToken.class.isAssignableFrom(authentication);
    }

}
