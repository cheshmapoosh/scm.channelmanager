package ir.daneshrefah.scm.common.dto.service.parent;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ParentServiceCreateRequest implements RequestData {

    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String title;
    private Integer version;
    @NotNull
    private ServiceStatus status;

}
