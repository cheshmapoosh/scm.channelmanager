package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
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
@RequestMapping("/protected/otp")
public class OtpController {

    private final OtpService otpService;

    @PostMapping(value = {"/services/mbuaa/api/register","/auth/activationRequest","/auth/register"})
    public void sendOtpSms(@RequestBody SmsOtpSendRequest request) {
        SecurityContextHolder.getContext().getAuthentication();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
//                .terminalCode()
//                .issuerUsername()
//                .recipient()
                .otpType(OtpType.SMS)
                .reason(request.getReason())
                .build();
        otpService.sendOtp(otpRequest);
    }

}
