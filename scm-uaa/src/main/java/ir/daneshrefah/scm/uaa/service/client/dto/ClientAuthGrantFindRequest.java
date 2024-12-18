package ir.daneshrefah.scm.uaa.service.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ClientAuthGrantFindRequest extends PagedRequestData {
    @NotNull
    @Numeric
    @Schema(description = "client numeric instance id")
    private Long clientId;
}
