package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;


import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.FirstLvlSmsRequestAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlSmsRequestAuthenticationProvider extends AbstractStaticRequestAuthenticationProvider{


    protected FirstLvlSmsRequestAuthenticationProvider(UserService userService, CustomMD5Encoder encoder, OtpService otpService) {
        super(userService, encoder, otpService);
    }

    @Override
    protected OtpType resolveOtpType() {
        return OtpType.SMS;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlSmsRequestAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
