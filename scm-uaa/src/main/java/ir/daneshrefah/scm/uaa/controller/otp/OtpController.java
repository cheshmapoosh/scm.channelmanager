package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.common.exception.MissingRequiredInputException;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.model.authentication.UserAuthentication;
import ir.daneshrefah.scm.uaa.common.model.user.User;
import ir.daneshrefah.scm.uaa.common.utils.AuthenticationUtils;
import ir.daneshrefah.scm.uaa.controller.BaseController;
import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.utils.string.StringUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;

import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestAccessParameter;
import static ir.daneshrefah.scm.uaa.utils.RequestUtils.extractRequestTerminalCode;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-02-05
 */
@RequiredArgsConstructor
@RestController
@RequestMapping("/otp")
public class OtpController extends BaseController {

    private final OtpService otpService;

    @PreAuthorize("isFullyAuthenticated()")
    @PostMapping("/sms")
    public OtpSendResponse sendOtpSms(@RequestBody SmsOtpSendRequest request, HttpServletRequest httpRequest) {

        User user = AuthenticationUtils.getLoggedInUser();
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));

        Recipient recipient = Recipient.builder()
                .address(request.getRecipient())
                .identifier(user.getNickname())
                .identifierType(UserIdentifierType.USER_NICKNAME)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();

        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();

        return otpService.sendOtp(otpRequest);
    }

    @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/sms-to")
    public OtpSendResponse sendDelegatedOtpSms(@RequestBody DelegatedSmsOtpSendRequest request) {

        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
//        if (!user.hasAuthority(ROLE_CSP)) {
//            throw new InvalidDelegationException(user.getName());
//        }
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));

        Recipient recipient = Recipient.builder()
                .address(request.getRecipient())
                .identifier(StringUtils.isNotBlank(request.getRecipientId()) ? request.getRecipientId() : request.getRecipient())
                .identifierType(Objects.nonNull(request.getRecipientIdType()) ? request.getRecipientIdType() : UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(StringUtils.isNotBlank(request.getTerminalCode()) ? request.getTerminalCode() : terminalCode)
                .accessParameter(StringUtils.isNotBlank(request.getAccessParameter()) ? request.getAccessParameter() : accessParameter)
                .build();

        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .recipient(recipient)
                .build();

        return otpService.sendOtp(otpRequest);
    }

    @PreAuthorize("isAnonymous()")
    @GetMapping("/public/sms-authentication/{recipient}")
    public OtpSendResponse sendAuthenticationOtpSms(@PathVariable("recipient") String recipientAddress) {

        UserAuthentication user = AuthenticationUtils.getLoggedInUserAuthentication();
        String terminalCode = extractRequestTerminalCode();
        String accessParameter = extractRequestAccessParameter().orElseThrow(() -> new MissingRequiredInputException("accessParameter"));

        Recipient recipient = Recipient.builder()
                .address(recipientAddress)
                .identifier(recipientAddress)
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(accessParameter)
                .build();

        OtpSendRequest otpRequest = OtpSendRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.AUTHENTICATION)
                .recipient(recipient)
                .build();

        return otpService.sendOtp(otpRequest);
    }

    @PostMapping("/verify")
    public OtpVerifyResponse verifyOtp(@RequestBody OtpVerifyRequest request, HttpServletRequest httpRequest) {
        User loggedInUser = AuthenticationUtils.getLoggedInUser();
        return otpService.verifyOtp(request);
    }

}
