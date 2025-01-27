package ir.daneshrefah.scm.common.dto.service.base;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ServiceRemoveBaseRequest implements RequestData {

    @NotNull
    @NotBlank
    public String id;
    @NotNull
    private LocalDateTime lastEditDate;

}
