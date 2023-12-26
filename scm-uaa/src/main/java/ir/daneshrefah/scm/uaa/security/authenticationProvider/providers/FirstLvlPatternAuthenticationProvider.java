package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlPatternAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlPatternAuthenticationProvider extends AbstractFirstLvlStaticAccessAuthenticationProvider {


    protected FirstLvlPatternAuthenticationProvider(CustomMD5Encoder encoder) {
        super(encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlPatternAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
