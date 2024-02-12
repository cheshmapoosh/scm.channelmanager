package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
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
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    @GetMapping("/sms")
    public void sendOtpSms() {
        SecurityContextHolder.getContext().getAuthentication();
        OtpSendRequest otpRequest = OtpSendRequest.builder()
//                .terminalCode()
//                .issuerUsername()
//                .recipient()
                .otpType(OtpType.SMS)
//                .reason(request.getReason())
                .build();
        otpService.sendOtp(otpRequest);
    }

}
