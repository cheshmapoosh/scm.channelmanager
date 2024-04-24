package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.uaa.common.exception.TwoStepAuthenticationRequiredException;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.security.token.GeneralAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.WebAttributes;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/public/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping("/sms")
    public OtpSendResponse sendOtpSms(@RequestBody SmsOtpSendRequest request, HttpServletRequest httpRequest) {
        User loggedInUser = extractLoggedInUser(httpRequest);
        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .issuerAddress(httpRequest.getRemoteHost())
                .issuerUser(AuthenticationUtils.getLoggedInUserAuthentication())
                .terminalCode(request.getTerminalCode())
                .accessParameter(null != loggedInUser ? StringUtils.join(loggedInUser.getAccessParameters().stream().toList(), ",") : null)
                .recipientUsername(request.getRecipientUsername())
                .recipient(request.getRecipient())
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .build();
        return otpService.sendOtp(otpRequest/*, loggedInUser*/);
    }

    @PostMapping("/verify")
    public OtpVerifyResponse verifyOtp(@RequestBody OtpVerifyRequest request, HttpServletRequest httpRequest) {
        User loggedInUser = extractLoggedInUser(httpRequest);
        return otpService.verifyOtp(request);
    }

    private User extractLoggedInUser(HttpServletRequest httpRequest) {
        User result = AuthenticationUtils.getLoggedInUser();
        if (null == result) {
            Exception exception = (Exception) httpRequest.getSession().getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
            Authentication authentication = null != exception && TwoStepAuthenticationRequiredException.class.isAssignableFrom(exception.getClass()) ?
                    ((TwoStepAuthenticationRequiredException) exception).getAuthentication() : null;
            result = null != authentication && authentication instanceof GeneralAuthenticationToken ?
                    ((GeneralAuthenticationToken) authentication).getPrincipal().getUser() : null;
        }
        return result;
    }

}
