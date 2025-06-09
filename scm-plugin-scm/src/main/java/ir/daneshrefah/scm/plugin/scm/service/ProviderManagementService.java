package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.provider.*;
import ir.daneshrefah.scm.common.dto.rest.ExternalProviderRequest;
import ir.daneshrefah.scm.common.dto.rest.ExternalProviderResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.service.AbstractAuditableExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Service
public class ProviderManagementService extends AbstractJavaService {

    private final ServiceService service;

    public ProviderManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ServiceService service) {
        super(producerTemplate, objectMapper);
        this.service = service;
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_LIST)
    public PagedResponseData<ServiceProviderFindResponse> serviceProviderList(ServiceProviderFindRequest request) {
        return this.service.findServiceProviderList(request);
    }


    @JavaService(operationCode = SVC_SERVICE_PROVIDER_NAME_LIST)
    @SuppressWarnings("unused")
    public List<ExternalProviderResponse> getServiceProviderNameList(ExternalProviderRequest request) {
        return this.service.getServiceProviderNameList(request);
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_BY_ID)
    public AbstractAuditableExternalServiceProvider findProviderById(String serviceProviderId) {
        AbstractAuditableExternalServiceProvider found = this.service.findServiceProviderById(serviceProviderId);
        if (Objects.nonNull(found)) {
            return found;
        }
        throw new NoMatchRecordFoundException("serviceProviderId");
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_CREATE)
    public AbstractAuditableExternalServiceProvider createServiceProvider(ServiceProviderCreteRequest request) {
       return service.createServiceProvider(request);
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_CHANGE)
    public AbstractAuditableExternalServiceProvider changeServiceProvider(ServiceProviderChangeRequest request) {
        return service.changeServiceProvider(request);
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_DELETE)
    public AbstractAuditableExternalServiceProvider deleteServiceProvider(ServiceProviderDeleteRequest request) {
        return service.deleteServiceProvider(request);
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_PROTOCOL_LIST)
    public List<String> findProviderProtocolList() {
        return Arrays.stream(ServiceProviderProtocol
                        .values())
                .map(ServiceProviderProtocol::name)
                .toList();
    }

    @JavaService(operationCode = SVC_SERVICE_PROVIDER_STATUS_LIST)
    public List<String> findProviderStatusList() {
        return Arrays.stream(ServiceProviderStatus
                        .values())
                .map(ServiceProviderStatus::name)
                .toList();
    }






}
