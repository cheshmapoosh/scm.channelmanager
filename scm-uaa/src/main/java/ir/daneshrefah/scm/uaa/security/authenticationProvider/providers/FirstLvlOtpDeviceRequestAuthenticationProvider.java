package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlOtpDeviceRequestAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.stereotype.Component;

@Component

public class FirstLvlOtpDeviceRequestAuthenticationProvider extends AbstractStaticRequestAuthenticationProvider{


    protected FirstLvlOtpDeviceRequestAuthenticationProvider(UserService userService, CustomMD5Encoder encoder, OtpService otpService) {
        super(userService, encoder, otpService);
    }

    @Override
    protected OtpType resolveOtpType() {
        return OtpType.DEVICE;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlOtpDeviceRequestAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
