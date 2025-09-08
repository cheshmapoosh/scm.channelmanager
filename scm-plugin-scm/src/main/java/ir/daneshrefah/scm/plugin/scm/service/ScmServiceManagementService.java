package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.dto.asset.EbService;
import ir.daneshrefah.scm.common.dto.service.EbServiceCreateRequest;
import ir.daneshrefah.scm.common.dto.service.EbServiceFilterRequest;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.service.ScmServiceService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.apache.camel.Header;
import org.springframework.stereotype.Component;

import static ir.daneshrefah.scm.common.constant.OperationCode.*;

@Component
public class ScmServiceManagementService extends AbstractJavaService {

    private final ScmServiceService scmServiceService;

    public ScmServiceManagementService(ServiceProducerTemplate producerTemplate,
                                       ObjectMapper objectMapper,
                                       ScmServiceService scmServiceService) {
        super(producerTemplate, objectMapper);
        this.scmServiceService = scmServiceService;
    }

    @JavaService(operationCode = SVC_SERVICE_LIST)
    public PagedResponseData<EbService> serviceList(EbServiceFilterRequest request) {
        return this.scmServiceService.findServiceList(request);
    }

    @JavaService(operationCode = SVC_SERVICE_BY_ID)
    public EbService findById(@Header("serviceId") Short serviceId) {
        return this.scmServiceService.findByServiceId(serviceId);
    }

    @JavaService(operationCode = SVC_SERVICE_CREATE)
    public EbService createService(EbServiceCreateRequest request) {
        return this.scmServiceService.createService(request);
    }

    @JavaService(operationCode = SVC_SERVICE_UPDATE)
    public EbService updateService(EbServiceCreateRequest request) {
        return this.scmServiceService.updateService(request);
    }
}
