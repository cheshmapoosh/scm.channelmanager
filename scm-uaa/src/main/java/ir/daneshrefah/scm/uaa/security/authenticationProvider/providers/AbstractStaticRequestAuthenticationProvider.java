package ir.daneshrefah.scm.uaa.security.authenticationProvider.providers;

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
import ir.daneshrefah.scm.utils.string.StringUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;

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
        requestOtp(authentication);
        PostAuthenticationToken result = PostAuthenticationToken.incomplete(
                authentication.getPrincipal(),
                authentication.getDetails());
        this.logger.debug("Authenticated user");
        return result;
    }

    protected void requestOtp(GeneralAuthenticationToken authentication) {
        OtpType otpType = resolveOtpType();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .issuerAddress(((WebAuthenticationDetails) authentication.getDetails().getDetails()).getRemoteAddress())
                .issuerUsername(authentication.getPrincipal().getUser().getNickname())
                .terminalCode(authentication.getPrincipal().getUser().getTerminalCode())
                .accessParameter(authentication.getDetails().getAccessParameter())
                .recipientUsername(authentication.getPrincipal().getUser().getNickname())
                .recipient(authentication.getPrincipal().getUser().getPerson().getMobile1())
                .otpType(otpType)
                .reason(OtpReason.AUTHENTICATION)
                .build();
        OtpSendResponse otpSendResponse = otpService.sendOtp(otpRequest);
    }

    protected abstract OtpType resolveOtpType();

    @Override
    public String extractCurrentPassword(TerminalUserDetails userDetails) {
        return userDetails.getPassword();
    }
}
