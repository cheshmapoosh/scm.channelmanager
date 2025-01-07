package ir.daneshrefah.scm.common.dto.service.parent;

import com.fasterxml.jackson.annotation.JsonIgnore;
import ir.daneshrefah.scm.common.annotation.RequestBodyModel;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@RequestBodyModel
public class ParentServiceCreateRequest implements RequestData {
    @JsonIgnore
    private final boolean isSystemic = false;
    private Integer version = 1;
    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String title;
    @NotBlankIfPresent
    private String alias;
    @NotNull
    private ServiceStatus status;
}
