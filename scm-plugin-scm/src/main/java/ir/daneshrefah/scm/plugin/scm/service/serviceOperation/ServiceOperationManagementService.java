package ir.daneshrefah.scm.plugin.scm.service.serviceOperation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.serviceOperation.ServiceOperationCreateRequest;
import ir.daneshrefah.scm.common.dto.serviceOperation.ServiceOperationResponse;
import ir.daneshrefah.scm.common.service.serviceOperation.ServiceOperationService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.apache.camel.Header;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServiceOperationManagementService extends AbstractJavaService {

    private final ServiceOperationService serviceOperationService;

    public ServiceOperationManagementService(ServiceProducerTemplate producerTemplate,
                                             ObjectMapper objectMapper,
                                             ServiceOperationService serviceOperationService) {
        super(producerTemplate, objectMapper);
        this.serviceOperationService = serviceOperationService;
    }

    @JavaService(operationCode = OperationCode.SCV_SERVICE_OPERATION_LIST)
    public List<ServiceOperationResponse> getAll(@Header("serviceId") Short serviceId) {
        return serviceOperationService.getAll(serviceId);
    }

    @JavaService(operationCode = OperationCode.SCV_SERVICE_OPERATION_CREATE)
    public List<ServiceOperationResponse> save(ServiceOperationCreateRequest request){
        return serviceOperationService.save(request);
    }
}