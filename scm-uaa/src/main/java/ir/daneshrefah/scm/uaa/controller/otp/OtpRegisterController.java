package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.uaa.domain.otp.RegisterDeviceResponse;
import ir.daneshrefah.scm.uaa.service.otp.OtpUserDeviceService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpRegisterDeviceRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/otp/register")
@RequiredArgsConstructor
public class OtpRegisterController {

    private final OtpUserDeviceService otpUserDeviceService;

    @PreAuthorize("hasAuthority(ROLE_CSP)")
    @PostMapping("/device")
    public RegisterDeviceResponse registerOtpDevice(@RequestBody OtpRegisterDeviceRequest request) {
        return otpUserDeviceService.registerOtpDevice(request);
    }
}
