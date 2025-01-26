package ir.daneshrefah.scm.uaa.service.client.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VersionCreateRequest implements RequestData {
    @NotNull
    private String version;
    private boolean isForced;
    @NotNull
    private String signature;
    @NotNull
    private ClientVersionStatus status;
    @NotNull
    @Numeric
    @Schema(description = "client numeric instance id")
    private Long clientId;
}
