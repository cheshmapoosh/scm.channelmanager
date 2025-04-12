package ir.daneshrefah.scm.common.constant.otp;

import ir.daneshrefah.scm.common.exception.InvalidInputException;
import ir.daneshrefah.scm.common.model.user.AuthenticationMethod;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2023-12-30
 */
public enum OtpType {
    TIME_BASED,
    DEVICE,
    EMAIL,
    SMS;

    public static OtpType toOtpType(AuthenticationMethod authenticationMethod) {
        return switch (authenticationMethod) {
            case OTP -> DEVICE;
            case SMS -> SMS;
            default -> throw new InvalidInputException("authenticationMethod");
        };
    }
}
