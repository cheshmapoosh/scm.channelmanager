package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import java.util.Objects;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM;


public abstract class AbstractOtpDeviceAuthenticationProvider extends AbstractAuthenticationProvider{

    private final OtpService otpService;

    public AbstractOtpDeviceAuthenticationProvider(UserService userService, OtpService otpService) {
        super(userService);
        this.otpService = otpService;
    }

    @Override
    protected void additionalAuthenticationChecks(TerminalUserDetails userDetails, GeneralAuthenticationToken authentication) throws AuthenticationException {
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .user(userDetails.getUser())
//                .accessParameter(authentication.getDetails().getAccessParameter())
//                .recipientUser(userDetails.getUser().getPerson())
//                .recipient(authentication.getPrincipal().getUser().getPerson().getMobile1())
                .otpType(OtpType.DEVICE)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(Recipient.builder().terminalCode(userDetails.getUser().getTerminalCode()).build())
                .claimCode(Objects.toString(authentication.getCredentials(), null))
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(request);
        if (!otpVerifyResponse.isSuccessful()) {
            throwError(authentication, new BadCredentialsException(OAUTH2_ERROR_CODE_INVALID_CLAIM));
        }
    }

    protected abstract void throwError(GeneralAuthenticationToken authentication, RuntimeException exception);

}
