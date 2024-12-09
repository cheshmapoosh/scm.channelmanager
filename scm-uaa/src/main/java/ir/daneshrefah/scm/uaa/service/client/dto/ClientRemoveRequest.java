package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClientRemoveRequest implements RequestData {
    @NotNull
    @Numeric
    private String clientId;
    @NotNull
    private LocalDateTime lastEditDate;

}
