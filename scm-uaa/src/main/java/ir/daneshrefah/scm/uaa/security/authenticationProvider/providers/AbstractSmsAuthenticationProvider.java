package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;


import ir.daneshrefah.scm.common.model.message.IssuerInfo;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.AuthenticationLevel;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;

import static ir.daneshrefah.scm.uaa.common.utils.Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM;

public abstract class AbstractSmsAuthenticationProvider extends AbstractAuthenticationProvider {

    private final OtpService otpService;

    public AbstractSmsAuthenticationProvider(UserService userService, OtpService otpService) {
        super(userService);
        this.otpService = otpService;
    }

    @Override
    protected void additionalAuthenticationChecks(TerminalUserDetails userDetails, GeneralAuthenticationToken authentication) throws AuthenticationException {
        Recipient recipient = Recipient.builder()
                .address(userDetails.getUser().getPerson().getMobile1())
//                .authenticationLevel(AuthenticationLevel.ANONYMOUS)
//                .identifier(userDetails.getUser().getNickname())
//                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(userDetails.getUser().getTerminalCode())
                .accessParameter(authentication.getDetails().getAccessParameter())
                .build();
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
//        private IssuerInfo issuer;
                .claimCode(authentication.getDetails().getClaimCode())

        //                .terminalCode(userDetails.getUser().getTerminalCode())
//                .accessParameter(authentication.getDetails().getAccessParameter())
//                .recipientUser(userDetails.getUser().getPerson())
//                .recipient(authentication.getPrincipal().getUser().getPerson().getMobile1())
                .build();
        OtpVerifyResponse otpVerifyResponse = otpService.verifyOtp(request);
        if (!otpVerifyResponse.isSuccessful()) {
            throwError(authentication, new BadCredentialsException(OAUTH2_ERROR_CODE_INVALID_CLAIM));
        }
    }

    protected abstract void throwError(GeneralAuthenticationToken authentication, Exception exception);

}
