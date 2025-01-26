package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ScopeRemoveRequest extends PagedRequestData {
    @Numeric
    private Long id;
    @NotNull
    private LocalDateTime lastEditDate;
}
