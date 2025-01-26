package ir.daneshrefah.scm.common.dto.service.parent;

import ir.daneshrefah.scm.common.annotation.RequestBodyModel;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@RequestBodyModel
public class ParentServiceEditRequest implements RequestData {
    @NotNull
    @NotBlank
    private String id;
    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String title;
    @NotBlankIfPresent
    private String alias;
    private boolean increaseVersion;
    @NotNull
    private ServiceStatus status;
    @NotNull
    private LocalDateTime lastEditDate;
}
