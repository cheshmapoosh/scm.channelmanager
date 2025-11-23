package ir.daneshrefah.scm.plugin.scm.service.definition;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.common.constant.OperationCode;
import ir.daneshrefah.scm.common.dto.definition.*;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.service.definition.DefinitionService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

@Service
public class DefinitionManagementService extends AbstractJavaService {

    private final DefinitionService definitionService;

    public DefinitionManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, DefinitionService definitionService) {
        super(producerTemplate, objectMapper);
        this.definitionService = definitionService;
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_LIST)
    public PagedResponseData<DefinitionResponse> getAllDefinitionsByTypes(DefinitionFilterRequest request) {
        return definitionService.getAllDefinitionsByTypes(request);
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_BY_ID)
    public DefinitionResponse getById(DefinitionFilterRequest request) {
        return definitionService.getDefinitionById(request.getId());
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_DETAILS_LIST_BY_ID)
    public DefinitionDetailResponse getAllDefinitionDetailsById(DefinitionDetailFilterRequest request) {
        return definitionService.getAllDefinitionDetailsById(request);
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_CREATE)
    public DefinitionResponse createDefinition(DefinitionRequest request){
        return definitionService.createDefinition(request);
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_UPDATE)
    public DefinitionResponse updateDefinition(DefinitionRequest request){
        return definitionService.updateDefinition(request);
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_DETAIL_CREATE)
    public DefinitionResponse createDefinitionDetail(DefinitionDetailRequest request){
        return definitionService.createDefinitionDetail(request);
    }

    @JavaService(operationCode = OperationCode.SVC_DEFINITION_DETAIL_UPDATE)
    public DefinitionResponse updateDefinitionDetail(DefinitionDetailRequest request){
        return definitionService.updateDefinitionDetail(request);
    }
}
