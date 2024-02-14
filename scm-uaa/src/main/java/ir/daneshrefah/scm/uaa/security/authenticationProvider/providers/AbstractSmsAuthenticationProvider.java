package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;


import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;

import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.security.core.AuthenticationException;

public abstract class AbstractSmsAuthenticationProvider extends AbstractAuthenticationProvider{

    private final OtpService otpService;

    public AbstractSmsAuthenticationProvider(UserService userService, OtpService otpService) {
        super(userService);
        this.otpService = otpService;
    }
//    private final OtpService otpService;

    @Override
    protected void additionalAuthenticationChecks(TerminalUserDetails userDetails, GeneralAuthenticationToken authentication) throws AuthenticationException {
        otpService.verifyOtp(null);
        //TODO check otp with otpService
//        boolean isClaimCodeVerified = otpService.verifyOtpByUsername(generalAuthenticationToken.getUser().getClient().getCode(),
//                generalAuthenticationToken.getName(),
//                generalAuthenticationToken.getAuthenticationRequest().getAccessParameter(),
//                generalAuthenticationToken.getAuthenticationRequest().getClaimCode(),
//                OtpReasonType.AUTHENTICATION);
//        if (!isClaimCodeVerified)
//            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
    }
}
