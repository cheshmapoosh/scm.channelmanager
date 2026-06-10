package ir.daneshrefah.scm.cmconnector.otp.model;

public record CmOtpVerifyRequest(
        String challengeId,
        String otp,
        String operation
) {
}
