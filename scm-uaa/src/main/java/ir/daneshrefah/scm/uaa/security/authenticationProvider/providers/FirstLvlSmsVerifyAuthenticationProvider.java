package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.token.FirstLvlSmsVerifyAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlSmsVerifyAuthenticationProvider extends AbstractSmsAuthenticationProvider{

    public FirstLvlSmsVerifyAuthenticationProvider(UserService userService, OtpService otpService) {
        super(userService, otpService);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlSmsVerifyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
