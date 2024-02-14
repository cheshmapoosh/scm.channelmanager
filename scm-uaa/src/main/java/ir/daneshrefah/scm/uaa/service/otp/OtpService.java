package ir.daneshrefah.scm.uaa.service.otp;

import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpSendRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.provder.OtpProvider;
import org.springframework.stereotype.Service;

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

    public boolean sendOtp(OtpSendRequest request) {
        return false;
    }

    public boolean verifyOtp(OtpVerifyRequest request) {
        return false;
    }

}
