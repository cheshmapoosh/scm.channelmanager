package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseChangeRequest implements RequestData {

    @NotNull
    @NotBlank
    private String id;
    private String transformerId;
    private String responseErrorCodeProperty;
    private String responseErrorMessageProperty;
    private String responseBodyType;
    @NotNull
    private LocalDateTime lastEditDate;
    @NotNull
    private Boolean enable;
    @NotBlank
    @NotNull
    private String title;

}
