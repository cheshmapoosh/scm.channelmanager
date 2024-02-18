package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.token.FirstLvlOtpDeviceVerifyAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlOtpDeviceVerifyAuthenticationProvider extends AbstractOtpDeviceAuthenticationProvider{

    public FirstLvlOtpDeviceVerifyAuthenticationProvider(UserService userService, OtpService otpService) {
        super(userService, otpService);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlOtpDeviceVerifyAuthenticationToken.class.isAssignableFrom(authentication);
    }

    @Override
    protected void throwError(GeneralAuthenticationToken authentication, Exception exception) {

    }

}
