package ir.daneshrefah.scm.common.dto.service.parent;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ParentServiceEditRequest implements RequestData {
    @NotNull
    @NotBlank
    private String id;
    @NotNull
    @NotBlank
    private String title;
    private Integer version;
    @NotNull
    private ServiceStatus status;
    @NotNull
    private LocalDateTime lastEditDate;

}
