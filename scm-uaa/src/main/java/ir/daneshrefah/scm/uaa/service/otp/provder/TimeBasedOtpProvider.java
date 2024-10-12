package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.domain.otp.OtpType;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Component
public class TimeBasedOtpProvider extends AbstractOtpProvider {

    public TimeBasedOtpProvider(CacheTemplate cacheTemplate, OtpProperties otpProperties, ProfileInfo profileInfo) {
        super(cacheTemplate, otpProperties,profileInfo);
    }

    @Override
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        return null;
    }

    @Override
    public OtpType getType() {
        return OtpType.TIME_BASED;
    }

}
