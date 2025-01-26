package ir.daneshrefah.scm.common.constant.otp;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
@Getter
public enum OtpReasonDictionary {

    //TODO ADD TO BUNDLE

    AUTHENTICATION("احراز هویت", "Authentication"),
    CHANGE_LOGIN_AUTHENTICATION_METHOD("تغییر شیوه رمز ورود", "Change login authentication method"),
    CHANGE_TRANSACTION_AUTHENTICATION_METHOD("تغییر شیوه رمز تراکنش", "Change transaction authentication method"),
    ACTIVATION("فعال سازی", "Activation"),
    UN_DEFINED("", "");

    private final String persian;
    private final String english;

    public static OtpReasonDictionary getOtpReasonDictionary(OtpReason otpReason) {
        return Arrays.stream(values())
                .filter(otpReasonDictionary -> otpReasonDictionary.name().equalsIgnoreCase(otpReason.name()))
                .findFirst()
                .orElse(UN_DEFINED);
    }
}
