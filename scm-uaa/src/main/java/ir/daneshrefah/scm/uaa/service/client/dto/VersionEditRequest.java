package ir.daneshrefah.scm.uaa.service.client.dto;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.validation.Numeric;
import ir.daneshrefah.scm.uaa.domain.client.ClientVersionStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class VersionEditRequest implements RequestData {
    @NotNull
    @Numeric
    private Long id;
    private String version;
    private boolean isForced;
    private String signature;
    private ClientVersionStatus status;
    @NotNull
    private LocalDateTime lastEditDate;
}
