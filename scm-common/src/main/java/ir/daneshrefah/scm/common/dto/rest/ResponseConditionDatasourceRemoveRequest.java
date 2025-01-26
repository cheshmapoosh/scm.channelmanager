package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseConditionDatasourceRemoveRequest implements RequestData {

    @NotNull
    @Numeric
    private Long id;
    @NotNull
    private LocalDateTime lastEditDate;
}
