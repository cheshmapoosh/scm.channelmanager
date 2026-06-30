package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSmsBasedNationalCodeRequest;
import ir.daneshrefah.scm.uaa.service.user.OtpUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShahkarOtpChallengeService {
    private final OtpUserService otpUserService;

    public void send(String nationalCode, String mobileNumber, String terminalCode) {
        OtpSmsBasedNationalCodeRequest request = new OtpSmsBasedNationalCodeRequest(
                nationalCode,
                PersonType.REAL,
                null
        );
        request.setReason(OtpReason.SHAHKAR_AUTHENTICATION);
        otpUserService.sendOtpSmsShahkar(request, terminalCode, mobileNumber);
    }
}
