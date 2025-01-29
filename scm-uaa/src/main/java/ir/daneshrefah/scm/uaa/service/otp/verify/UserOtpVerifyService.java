package ir.daneshrefah.scm.uaa.service.otp.verify;

import ir.daneshrefah.scm.uaa.service.otp.dto.*;

public interface UserOtpVerifyService {

    OtpVerifyResponse verifyOtpByDelegatedUser(VerifyOtpByDelegatedUserRequest request);

    OtpVerifyResponse verifyOtpByLoggedInUser(VerifyOtpByLoggedInUserRequest request);

    OtpVerifyResponse verifyOtpByUsername(VerifyOtpByUsernameRequest request);

    OtpVerifyResponse verifyOtpByNickname(VerifyOtpByNicknameRequest request);

    OtpVerifyResponse verifyOtpByNationalCode(VerifyOtpByNationalCodeRequest request);
}
