package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ScopeEditRequest extends PagedRequestData {
    @Numeric
    private Long id;
    @NotNull
    @NotBlank
    private String code;
    @NotNull
    @NotBlank
    private String title;
    @NotNull
    private LocalDateTime lastEditDate;
}
