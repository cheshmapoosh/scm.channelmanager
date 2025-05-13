package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.provider.ServiceProviderFindRequest;
import ir.daneshrefah.scm.common.dto.provider.ServiceProviderFindResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;

import java.util.List;

public interface ServiceProviderService {

    List<AbstractAuditableExternalServiceProvider> findServiceProviderList();

    PagedResponseData<ServiceProviderFindResponse> findServiceProviderList(ServiceProviderFindRequest request);

    AbstractAuditableExternalServiceProvider findServiceProviderById(String id);

    AbstractAuditableExternalServiceProvider findServiceProviderByCode(String code);

    AbstractAuditableExternalServiceProvider findServiceProviderByIdOrCode(String value);

    boolean checkServiceProviderExistById(String serviceProviderId);

}
