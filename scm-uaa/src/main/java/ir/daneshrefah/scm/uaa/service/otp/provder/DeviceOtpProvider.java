package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.service.otp.OtpDeviceService;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyRequest;
import ir.daneshrefah.scm.uaa.service.otp.dto.OtpVerifyResponse;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
@Slf4j
@Component
public class DeviceOtpProvider extends AbstractOtpProvider {

    @Autowired
    private OtpDeviceService otpDeviceService;

    public DeviceOtpProvider(CacheManager cacheManager, OtpProperties otpProperties, ProfileInfo profileInfo) {
        super(cacheManager, otpProperties, profileInfo);
    }

    @Override
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        return otpDeviceService.verifyOtp(request);
    }

    @Override
    public OtpType getType() {
        return OtpType.DEVICE;
    }

}
