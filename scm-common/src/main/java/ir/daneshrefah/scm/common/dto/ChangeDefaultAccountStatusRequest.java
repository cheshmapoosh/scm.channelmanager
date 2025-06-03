package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.person.PersonType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChangeDefaultAccountStatusRequest implements RequestData {
    @NotNull
    private PersonType personType;
    @NotNull
    @NotBlank
    private String nationalId;
    @NotBlankIfPresent
    private String subOrganizationId;
    @NotNull
    @NotBlank
    private String accountNo;

}
