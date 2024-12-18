package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseDeleteRequest implements RequestData {

    @NotNull
    @NotBlank
    private String id;
    @NotNull
    private LocalDateTime lastEditDate;
}
