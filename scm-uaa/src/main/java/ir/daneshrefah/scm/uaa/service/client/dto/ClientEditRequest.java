package ir.daneshrefah.scm.uaa.service.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ClientEditRequest extends ClientCreateRequest {
    @NotNull
    @Numeric
    @Schema(description = "client numeric instance id")
    private Long id;
    @NotNull
    private LocalDateTime lastEditDate;

}
