package ir.daneshrefah.scm.cmconnector.otp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CmOtpVerifyRequest(
        @NotBlank
        @Size(max = 128)
        String challengeId,

        @NotBlank
        @Pattern(regexp = "\\d{4,8}")
        String otp,

        @NotBlank
        @Size(max = 100)
        String operation
) {
}
