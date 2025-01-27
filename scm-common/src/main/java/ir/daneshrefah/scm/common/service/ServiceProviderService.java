package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.provider.ServiceProviderFindRequest;
import ir.daneshrefah.scm.common.dto.provider.ServiceProviderFindResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;

import java.util.List;

public interface ServiceProviderService {

    List<AbstractExternalServiceProvider> findServiceProviderList();

    PagedResponseData<ServiceProviderFindResponse> findServiceProviderList(ServiceProviderFindRequest request);

    AbstractExternalServiceProvider findServiceProviderById(String id);

    AbstractExternalServiceProvider findServiceProviderByCode(String code);

    AbstractExternalServiceProvider findServiceProviderByIdOrCode(String value);

    boolean checkServiceProviderExistById(String serviceProviderId);

}
