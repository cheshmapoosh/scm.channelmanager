package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.token.FirstLvlOtpDeviceVerifyAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.stereotype.Component;

@Component
public class FirstLvlOtpDeviceVerifyAuthenticationProvider extends AbstractOtpDeviceAuthenticationProvider{

    public FirstLvlOtpDeviceVerifyAuthenticationProvider(UserService userService) {
        super(userService);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return FirstLvlOtpDeviceVerifyAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
