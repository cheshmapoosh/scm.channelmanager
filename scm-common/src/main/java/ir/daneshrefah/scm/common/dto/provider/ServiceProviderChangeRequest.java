package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ServiceProviderChangeRequest  implements RequestData {

    @NotBlank
    private String serviceProviderId;
    @NotNull
    private LocalDateTime lastEditDate;
    @NotBlankIfPresent
    private String code;
    @NotBlankIfPresent
    private String title;
    @NotBlankIfPresent
    private String providerClassName;
    @NotNull
    private ServiceProviderStatus status;
    @NotBlankIfPresent
    private String assetProviderId;
}
