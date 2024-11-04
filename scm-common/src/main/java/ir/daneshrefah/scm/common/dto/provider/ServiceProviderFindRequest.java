package ir.daneshrefah.scm.common.dto.provider;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ServiceProviderFindRequest extends PagedRequestData {

    private String id;
    private String code;
    private String title;
    private ServiceProviderProtocol protocol;
    private String providerClassName;
}
