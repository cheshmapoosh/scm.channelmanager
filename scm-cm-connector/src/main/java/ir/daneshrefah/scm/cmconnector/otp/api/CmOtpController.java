package ir.daneshrefah.scm.cmconnector.otp.api;

import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyRequest;
import ir.daneshrefah.scm.cmconnector.otp.model.CmOtpVerifyResponse;
import ir.daneshrefah.scm.cmconnector.otp.service.CmOtpService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/cm/v1/otp")
@RequiredArgsConstructor
public class CmOtpController {
    private final CmOtpService otpService;

    @PostMapping("/verify")
    public ResponseEntity<CmOtpVerifyResponse> verify(@Valid @RequestBody CmOtpVerifyRequest request) {
        return ResponseEntity.ok(otpService.verify(request));
    }
}
