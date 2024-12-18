package ir.daneshrefah.scm.uaa.service.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientRemoveRequest implements RequestData {
    @NotNull
    @Schema(description = "client numeric instance id")
    private Long clientId;
    @NotNull
    private LocalDateTime lastEditDate;

}
