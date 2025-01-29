package ir.daneshrefah.scm.uaa.controller.otp;

import ir.daneshrefah.scm.common.model.person.GeneralPerson;
import ir.daneshrefah.scm.uaa.domain.otp.OtpAssignRequest;
import ir.daneshrefah.scm.uaa.service.otp.OtpAssignService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/otp/assign")
@RequiredArgsConstructor
public class OtpDeviceAssignController {

    private final OtpAssignService otpAssignService;

    @PostMapping
    public List<GeneralPerson> findOtpRegistration(@RequestBody OtpAssignRequest otpAssignRequest) {
        return otpAssignService.findOtpRegistration(otpAssignRequest);
    }
}
