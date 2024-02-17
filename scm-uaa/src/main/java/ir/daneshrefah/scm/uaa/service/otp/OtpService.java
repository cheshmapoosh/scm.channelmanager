package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.uaa.domain.otp.OtpReason;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendResponse;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.service.otp.provder.OtpProvider;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Service
public class OtpService {

    private final Map<OtpType, OtpProvider> providers;

    public OtpService(List<OtpProvider> providers) {
        this.providers = providers.stream()
                .collect(Collectors.toMap(OtpProvider::getType, Function.identity()));
    }

    public OtpSendResponse sendOtp(OtpSendRequest request) {
        System.out.println("sending otp request");
        return OtpSendResponse.builder()
                .terminalCode(request.getTerminalCode())
                .accessParameter(request.getAccessParameter())
                .recipientUsername(request.getRecipientUsername())
                .recipient(request.getRecipient())
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .isSuccessful(true)
                .otpCode("456")
                .expireTime(Instant.now())
                .build();
    }

    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        System.out.println("verify otp request");
        return OtpVerifyResponse.builder()
                .terminalCode(request.getTerminalCode())
                .accessParameter(request.getAccessParameter())
                .recipientUsername(request.getRecipientUsername())
                .recipient(request.getRecipient())
                .otpType(request.getOtpType())
                .reason(request.getReason())
                .isSuccessful("456".equals(request.getClaimCode()))
                .tryCount(1)
                .build();
    }

}
