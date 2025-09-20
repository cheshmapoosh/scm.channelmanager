package ir.daneshrefah.scm.plugin.scm.service.operation;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.operation.OperationFilterRequest;
import ir.daneshrefah.scm.common.dto.operation.OperationResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.service.operation.OperationService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Component;

@Component
public class OperationManagementService extends AbstractJavaService {

    private final OperationService operationService;

    public OperationManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, OperationService operationService) {
        super(producerTemplate, objectMapper);
        this.operationService = operationService;
    }

    @JavaService(operationCode = OperationCode.SVC_OPERATION_LIST)
    public PagedResponseData<OperationResponse> getOperationService(OperationFilterRequest request) {
        return operationService.getAllOperationsByFilter(request);
    }


}
