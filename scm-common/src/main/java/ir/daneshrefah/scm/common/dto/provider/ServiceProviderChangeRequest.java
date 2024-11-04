package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Setter
@Getter
public class ServiceProviderChangeRequest  implements RequestData {

    private String serviceProviderId;
    private LocalDateTime lastEditDate;
    private String code;
    private String title;
    private String providerClassName;
    private ServiceProviderStatus status;
    private String assetProviderId;
}
