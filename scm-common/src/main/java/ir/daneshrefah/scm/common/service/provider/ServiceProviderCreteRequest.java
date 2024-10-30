package ir.daneshrefah.scm.common.service.provider;

import ir.daneshrefah.scm.common.BaseModel;
import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.HttpContentType;
import ir.daneshrefah.scm.common.model.service.HttpMethod;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
