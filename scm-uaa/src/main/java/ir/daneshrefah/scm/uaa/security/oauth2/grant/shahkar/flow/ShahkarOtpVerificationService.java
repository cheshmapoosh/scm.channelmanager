package ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.flow;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.recipient.Recipient;
import ir.daneshrefah.scm.common.model.user.UserIdentifierType;
import ir.daneshrefah.scm.uaa.common.utils.Constants;
import ir.daneshrefah.scm.uaa.security.oauth2.grant.shahkar.ShahkarGrantAuthenticationToken;
import ir.daneshrefah.scm.uaa.service.otp.OtpService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import static ir.daneshrefah.scm.uaa.common.utils.ErrorUtils.throwError;

@Service
@RequiredArgsConstructor
public class ShahkarOtpVerificationService {
    private final OtpService otpService;

    public void verify(ShahkarGrantAuthenticationToken token, String terminalCode) {
        String mobileNumber = token.getPhoneNumber();
        Recipient recipient = Recipient.builder()
                .address(mobileNumber)
                .identifier(mobileNumber)
                .identifierType(UserIdentifierType.MOBILE_NUMBER)
                .terminalCode(terminalCode)
                .accessParameter(token.getAccessParameter())
                .build();
        OtpVerifyRequest request = OtpVerifyRequest.builder()
                .otpType(OtpType.SMS)
                .reason(OtpReason.SHAHKAR_AUTHENTICATION)
                .recipient(recipient)
                .claimCode(token.getActivationCode())
                .build();
        OtpVerifyResponse response = otpService.verifyOtp(request);
        if (!response.isSuccessful()) {
            throwError(Constants.OAUTH2_ERROR_CODE_INVALID_CLAIM, Constants.PWA_OTP_CODE_HEADER);
        }
    }
}
