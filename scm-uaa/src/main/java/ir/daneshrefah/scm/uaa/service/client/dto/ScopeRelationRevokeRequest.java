package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ScopeRelationRevokeRequest implements RequestData {
    @NotNull
    @Numeric
    private Long scopeId;
    @NotNull
    @Numeric
    private Long clientId;
    @NotNull
    private LocalDateTime lastEditDate;
}
