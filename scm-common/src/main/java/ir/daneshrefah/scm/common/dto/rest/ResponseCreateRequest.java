package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ResponseCreateRequest implements RequestData {

    @NotBlankIfPresent
    private String transformerId;
    @NotBlankIfPresent
    private String serviceId;
    @NotBlankIfPresent
    private String serviceProviderId;
    @NotBlankIfPresent
    private String responseErrorCodeProperty;
    @NotBlankIfPresent
    private String responseErrorMessageProperty;
    @NotNull
    private ExternalServiceBodyType responseBodyType;
    @NotNull
    private Boolean enable;
    @NotNull
    @NotBlank
    private String title;
}
