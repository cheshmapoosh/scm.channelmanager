package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.token.FirstLvlSmsVerifyAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlSmsVerifyAuthenticationProvider extends AbstractSmsAuthenticationProvider{

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlSmsVerifyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
