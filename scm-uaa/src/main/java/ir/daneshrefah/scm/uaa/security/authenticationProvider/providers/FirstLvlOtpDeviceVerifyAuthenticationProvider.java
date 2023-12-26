package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.token.FirstLvlOtpDeviceVerifyAuthenticationToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlOtpDeviceVerifyAuthenticationProvider extends AbstractOtpDeviceAuthenticationProvider{

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlOtpDeviceVerifyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
