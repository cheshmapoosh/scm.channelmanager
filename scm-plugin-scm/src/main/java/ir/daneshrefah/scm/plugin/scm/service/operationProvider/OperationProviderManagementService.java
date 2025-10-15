package ir.daneshrefah.scm.plugin.scm.service.operationProvider;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.operationProvide.OperationProviderResponse;
import ir.daneshrefah.scm.common.service.operationProvider.OperationProviderService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OperationProviderManagementService extends AbstractJavaService {

    private final OperationProviderService operationProviderService;

    public OperationProviderManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, OperationProviderService operationProviderService) {
        super(producerTemplate, objectMapper);
        this.operationProviderService = operationProviderService;
    }

    @JavaService(operationCode = OperationCode.SVC_OPERATION_PROVIDER_LIST)
    public List<OperationProviderResponse> findAll(){
        return operationProviderService.findAll();
    }
}