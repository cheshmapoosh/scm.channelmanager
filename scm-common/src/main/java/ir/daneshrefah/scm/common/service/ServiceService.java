package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.*;
import ir.daneshrefah.scm.common.dto.provider.ServiceProviderChangeRequest;
import ir.daneshrefah.scm.common.dto.provider.ServiceProviderCreteRequest;
import ir.daneshrefah.scm.common.dto.provider.ServiceProviderDeleteRequest;
import ir.daneshrefah.scm.common.dto.rest.ExternalProviderRequest;
import ir.daneshrefah.scm.common.dto.rest.ExternalProviderResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ProviderTerminalCoding;
import ir.daneshrefah.scm.common.model.service.ScmService;

import java.util.List;
import java.util.Optional;

/**
 * Description of the class or purpose of the file.
 *
 * @author reza jamshidi
 * @version 1.0
 * @since 2024-01-17
 */
public interface ServiceService extends
        ServiceProviderService,
        JavaServicesService,
        PatentServicesService,
        CompositionServicesService {

    List<ScmService> findServiceList();

    PagedResponseData<ScmService> findServiceList(ServiceFindRequest request);

    ScmService findServiceByCode(String code);

    ScmService findServiceById(String id);

    ScmService createService(ServiceInfoRequest service);

    ScmService updateService(ServiceInfoEditRequest request);

    boolean checkServiceExistById(String serviceId);

    void deleteService(ServiceDeleteRequest request);

    PagedResponseData<TerminalServiceAccessAssignmentResponse> findAllServiceAccessOnTerminal(ServiceAccessFindRequest request);

    Optional<ProviderTerminalCoding> findProviderTerminalCoding(String terminalCode, String clientId, String providerCode);

    AbstractAuditableExternalServiceProvider createServiceProvider(ServiceProviderCreteRequest request);

    AbstractAuditableExternalServiceProvider deleteServiceProvider(ServiceProviderDeleteRequest request);

    AbstractAuditableExternalServiceProvider changeServiceProvider(ServiceProviderChangeRequest request);

    List<ExternalProviderResponse> getServiceProviderNameList(ExternalProviderRequest request);

    void cacheEvict();

}
