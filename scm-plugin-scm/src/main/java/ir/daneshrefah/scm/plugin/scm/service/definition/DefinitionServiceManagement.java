package ir.daneshrefah.scm.plugin.scm.service.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.definition.DefinitionDetailResponse;
import ir.daneshrefah.scm.common.dto.definition.DefinitionFilterRequest;
import ir.daneshrefah.scm.common.dto.definition.DefinitionDetailRequest;
import ir.daneshrefah.scm.common.dto.definition.DefinitionResponse;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

@Service
public class DefinitionServiceManagement extends AbstractJavaService {

    private final DefinitionService definitionService;

    public DefinitionServiceManagement(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, DefinitionService definitionService) {
        super(producerTemplate, objectMapper);
        this.definitionService = definitionService;
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_LIST)
    public PagedResponseData<DefinitionResponse> getAllDefinitionsByTypes(DefinitionFilterRequest request) {
        return definitionService.getAllDefinitionsByTypes(request);
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_DETAILS_LIST_BY_ID)
    public DefinitionDetailResponse getAllDefinitionDetailsById(DefinitionDetailRequest request) {
        return definitionService.getAllDefinitionDetailsById(request);
    }
}
