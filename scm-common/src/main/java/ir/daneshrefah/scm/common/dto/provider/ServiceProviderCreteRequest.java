package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceProviderCreteRequest implements RequestData {

    private String code;
    private String title;
    private String providerClassName;
    private ServiceProviderProtocol protocol;
    private ServiceProviderStatus status;
    private String assetProviderId;
    //METADATA HANDLED BY PARAMETER

}
