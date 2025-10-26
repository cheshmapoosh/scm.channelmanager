package ir.daneshrefah.scm.uaa.service.activation.pwa.model;

import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
@Builder
public class ActivationRequest {
    @NotNull
    @NotBlank
    @Pattern(regexp = "[a-zA-z0-9.]{6,25}")
    private String username;
    @NotNull
    @NotBlank
    @Pattern(regexp = "09[0-9]{9}")
    private String phoneNumber;
    @NotBlankIfPresent
    private String otpCode;
    private ActivationRequestHeader requestHeaders;
}
