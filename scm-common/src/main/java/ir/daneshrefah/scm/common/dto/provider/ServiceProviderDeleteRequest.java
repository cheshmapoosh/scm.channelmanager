package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ServiceProviderDeleteRequest implements RequestData {
    @NotNull
    private LocalDateTime lastEditDate;
    @NotBlank
    private String serviceProviderId;
}
