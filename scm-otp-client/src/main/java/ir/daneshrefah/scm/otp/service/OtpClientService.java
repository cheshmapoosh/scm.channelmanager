package ir.daneshrefah.scm.otp.service;

import ir.daneshrefah.scm.common.constant.otp.OtpReason;

public interface OtpClientService {

    boolean verifyOtpOrStaticPasswordLoggedInUser(String authorization, String otpCode, OtpReason reason, String accessParameter);

    void verifyOtpOrStaticPasswordLoggedInUserWithException(String authorization, String otpCode, OtpReason reason, String accessParameter);
}
