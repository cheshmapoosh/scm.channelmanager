package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlOtpDeviceRequestAuthenticationToken;
import org.springframework.stereotype.Component;

@Component

public class FirstLvlOtpDeviceRequestAuthenticationProvider extends AbstractStaticRequestAuthenticationProvider{


    protected FirstLvlOtpDeviceRequestAuthenticationProvider(CustomMD5Encoder encoder) {
        super(encoder);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlOtpDeviceRequestAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
