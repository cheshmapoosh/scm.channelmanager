package ir.daneshrefah.scm.common.dto.service.parent;

import ir.daneshrefah.scm.common.annotation.RequestBodyModel;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@RequestBodyModel
public class ParentServiceRemoveRequest implements RequestData {
    @NotNull
    @NotBlank
    public String id;
    @NotNull
    private LocalDateTime lastEditDate;
}
