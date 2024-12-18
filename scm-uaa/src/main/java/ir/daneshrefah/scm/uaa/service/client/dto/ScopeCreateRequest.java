package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ScopeCreateRequest implements RequestData {

    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String title;
}
