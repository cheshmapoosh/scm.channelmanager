package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.provider.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.exception.NoMatchRecordFoundException;
import ir.daneshrefah.scm.common.model.service.AbstractExternalServiceProvider;
import ir.daneshrefah.scm.common.model.service.ServiceProviderProtocol;
import ir.daneshrefah.scm.common.model.service.ServiceProviderStatus;
import ir.daneshrefah.scm.common.service.ServiceService;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service
public class ProviderManagementService extends AbstractJavaService {

    private final ServiceService service;

    public ProviderManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ServiceService service) {
        super(producerTemplate, objectMapper);
        this.service = service;
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_LIST")
    public PagedResponseData<ServiceProviderFindResponse> serviceProviderList(ServiceProviderFindRequest request) {
        return this.service.findServiceProviderList(request);
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_BY_ID")
    public AbstractExternalServiceProvider findProviderById(String serviceProviderId) {
        AbstractExternalServiceProvider found = this.service.findServiceProviderById(serviceProviderId);
        if (Objects.nonNull(found)) {
            return found;
        }
        throw new NoMatchRecordFoundException("serviceProviderId");
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_CREATE")
    public AbstractExternalServiceProvider createServiceProvider(ServiceProviderCreteRequest request) {
       return service.createServiceProvider(request);
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_CHANGE")
    public AbstractExternalServiceProvider changeServiceProvider(ServiceProviderChangeRequest request) {
        return service.changeServiceProvider(request);
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_DELETE")
    public AbstractExternalServiceProvider deleteServiceProvider(ServiceProviderDeleteRequest request) {
        return service.deleteServiceProvider(request);
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_PROTOCOL_LIST")
    public List<String> findProviderProtocolList() {
        return Arrays.stream(ServiceProviderProtocol
                        .values())
                .map(ServiceProviderProtocol::name)
                .toList();
    }

    @JavaService(serviceCode = "SVC_SERVICE_PROVIDER_STATUS_LIST")
    public List<String> findProviderStatusList() {
        return Arrays.stream(ServiceProviderStatus
                        .values())
                .map(ServiceProviderStatus::name)
                .toList();
    }






}
