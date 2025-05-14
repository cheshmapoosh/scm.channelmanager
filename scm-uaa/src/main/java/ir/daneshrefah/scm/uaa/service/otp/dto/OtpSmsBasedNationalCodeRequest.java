package ir.daneshrefah.scm.uaa.service.otp.dto;

import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import ir.daneshrefah.scm.uaa.controller.otp.SmsOtpSendRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class OtpSmsBasedNationalCodeRequest extends SmsOtpSendRequest {
    @NotNull
    @NotBlank
    private String nationalId;
    @NotNull
    private PersonType personType;
    @NotBlankIfPresent
    private String subOrg;
}
