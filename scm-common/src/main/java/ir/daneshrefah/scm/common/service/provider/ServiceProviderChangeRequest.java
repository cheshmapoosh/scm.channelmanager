package ir.daneshrefah.scm.common.service.provider;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

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
    //GENERAL METADATA
    private String endpoint;
    private Integer connectTimeout;
    private Integer responseTimeout;
    private Integer soTimeout;
    private List<ServiceProviderData> additionalParams;

    //REST METADATA
    private HttpMethod defaultHttpMethod;
    private HttpContentType defaultRequestContentType;
}
