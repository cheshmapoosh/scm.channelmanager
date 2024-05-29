package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.security.authenticationDetails.TerminalUserDetails;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.security.CustomMD5Encoder;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.security.token.PostAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import org.springframework.security.core.Authentication;

public abstract class AbstractStaticRequestAuthenticationProvider extends AbstractStaticAuthenticationProvider {

    private final OtpService otpService;

    protected AbstractStaticRequestAuthenticationProvider(UserService userService, CustomMD5Encoder encoder, OtpService otpService) {
        super(userService, encoder);
        this.otpService = otpService;
    }

    protected Authentication createSuccessAuthentication(GeneralAuthenticationToken authentication) {
        // Ensure we return the original credentials the user supplied,
        // so subsequent attempts are successful even with encoded passwords.
        // Also ensure we return the original getDetails(), so that future
        // authentication events after cache expiry contain the details
        OtpSendResponse otpSendResponse = requestOtp(authentication);
        PostAuthenticationToken result = PostAuthenticationToken.incomplete(
                authentication.getPrincipal(),
                authentication.getDetails(),
                otpSendResponse);
        this.logger.debug("Authenticated user");
        return result;
    }

    protected OtpSendResponse requestOtp(GeneralAuthenticationToken authentication) {
        OtpType otpType = resolveOtpType();
        User user = authentication.getPrincipal().getUser();
        Recipient recipient = Recipient.builder()
                .address(user.getPerson().getMobile1())
                .identifier(user.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(user.getTerminalCode())
                .accessParameter(authentication.getDetails().getAccessParameter())
                .build();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(otpType)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
                .build();
        return otpService.sendOtp(otpRequest/*, user*/);
    }

    protected abstract OtpType resolveOtpType();

    @Override
    public String extractCurrentPassword(TerminalUserDetails userDetails) {
        return userDetails.getPassword();
    }

}
