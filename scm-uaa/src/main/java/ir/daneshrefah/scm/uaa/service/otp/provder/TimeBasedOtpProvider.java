package ir.daneshrefah.scm.uaa.service.otp.provder;

import ir.daneshrefah.scm.cache.client.connector.CacheTemplate;
import ir.daneshrefah.scm.common.constant.otp.OtpType;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;
import ir.daneshrefah.scm.uaa.config.OtpProperties;
import ir.daneshrefah.scm.uaa.service.otp.dto.*;
import ir.daneshrefah.scm.uaa.service.user.UserService;
import ir.daneshrefah.scm.uaa.utils.ProfileInfo;
import org.springframework.context.annotation.Lazy;
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

    public TimeBasedOtpProvider(CacheTemplate cacheTemplate, OtpProperties otpProperties, ProfileInfo profileInfo, @Lazy UserService userService) {
        super(cacheTemplate, otpProperties, profileInfo, userService);
    }

    @Override
    public OtpVerifyResponse verifyOtp(OtpVerifyRequest request) {
        return null;
    }

    @Override
    public OtpVerifyResponse verifyOtpByDelegatedUser(VerifyOtpByDelegatedUserRequest request) {
        return null;
    }

    @Override
    public OtpVerifyResponse verifyOtpByLoggedInUser(VerifyOtpByLoggedInUserRequest request) {
        return null;
    }

    @Override
    public OtpVerifyResponse verifyOtpByUsername(VerifyOtpByUsernameRequest request) {
        return null;
    }

    @Override
    public OtpVerifyResponse verifyOtpByNickname(VerifyOtpByNicknameRequest request) {
        return null;
    }

    @Override
    public OtpType getType() {
        return OtpType.TIME_BASED;
    }
}
