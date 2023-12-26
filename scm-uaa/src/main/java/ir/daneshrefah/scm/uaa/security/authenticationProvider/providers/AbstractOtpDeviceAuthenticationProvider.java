package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.userDetails.TerminalUserDetails;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2ErrorCodes;

public abstract class AbstractOtpDeviceAuthenticationProvider extends AbstractAuthenticationProvider{
//    private final OtpService otpService;


    @Override
    protected void additionalAuthenticationChecks(TerminalUserDetails userDetails, GeneralAuthenticationToken authentication) throws AuthenticationException {
        //TODO check otp with otpService
//        boolean isClaimCodeVerified = otpService.verifyOtpByUsername(generalAuthenticationToken.getUser().getClient().getCode(),
//                generalAuthenticationToken.getName(),
//                generalAuthenticationToken.getAuthenticationRequest().getAccessParameter(),
//                generalAuthenticationToken.getAuthenticationRequest().getClaimCode(),
//                OtpReasonType.AUTHENTICATION);
//        if (isClaimCodeVerified)
//            throw new OAuth2AuthenticationException(OAuth2ErrorCodes.ACCESS_DENIED);
    }
}
