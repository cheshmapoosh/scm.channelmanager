package ir.daneshrefah.scm.cmconnector.otp.model;

public record CmOtpVerifyResponse(
        boolean verified,
        String verificationToken,
        long expiresInSeconds
) {
}
