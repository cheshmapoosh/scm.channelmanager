package ir.daneshrefah.scm.uaa.domain.pwa;

import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class ActivationRequestDto implements Serializable {
    @NotNull
    @NotBlank
    private String phoneNumber;
    @NotNull
    @NotBlank
    private String username;
    @NotBlankIfPresent
    private String activationCode;
}
