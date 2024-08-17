package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;
import ir.daneshrefah.scm.common.service.rest.*;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicRestManagementService extends AbstractJavaService {

    private final ParameterService parameterService;

    public DynamicRestManagementService
            (
                    ServiceProducerTemplate producerTemplate,
                    ObjectMapper objectMapper,
                    ParameterService parameterService
            ) {
        super(producerTemplate, objectMapper);
        this.parameterService = parameterService;
    }


    @JavaService(serviceCode = "SVC_PARAMETER_CREATE")
    @SuppressWarnings("unused")
    public Parameter createParameter(ParameterCreateRequest request) {
        return parameterService.create(request);
    }

    @JavaService(serviceCode = "SVC_PARAMETER_CHANGE")
    @SuppressWarnings("unused")
    public Parameter changeParameter(ParameterChangeRequest request) {
        return parameterService.change(request);
    }

    @JavaService(serviceCode = "SVC_PARAMETER_DELETE")
    @SuppressWarnings("unused")
    public Parameter removeParameter(ParameterDeleteRequest request) {
        return parameterService.remove(request);
    }

    @JavaService(serviceCode = "SVC_PARAMETER_FIND_BY_ID")
    @SuppressWarnings("unused")
    public Parameter findParameterById(String id) {
        return parameterService.findParameterById(id);
    }

    @JavaService(serviceCode = "SVC_PARAMETER_FIND")
    @SuppressWarnings("unused")
    public PagedResponseData<Parameter> findParameter(ParameterFindRequest request) {
        return parameterService.findParameter(request);
    }

    @JavaService(serviceCode = "SVC_PARAMETER_FIND_TREE")
    @SuppressWarnings("unused")
    public ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request) {
        return parameterService.findParameterTree(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_FIND")
    @SuppressWarnings("unused")
    public PagedResponseData<ResponseCondition> findResponseCondition(ResponseConditionFindRequest request) {
        return parameterService.findResponseCondition(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_CREATE")
    @SuppressWarnings("unused")
    public ResponseCondition createResponseCondition(ResponseConditionRequest request) {
        return parameterService.createResponseCondition(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_CHANGE")
    @SuppressWarnings("unused")
    public ResponseCondition changeResponseCondition(ResponseConditionChangeRequest request) {
        return parameterService.changeResponseCondition(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_DELETE")
    @SuppressWarnings("unused")
    public ResponseCondition removeResponseCondition(ResponseConditionDeleteRequest request) {
        return parameterService.removeResponseCondition(request);
    }

    @JavaService(serviceCode = "SVC_CONDITION_DATA_SOURCE_CREATE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request) {
        return parameterService.createResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_CONDITION_DATA_SOURCE_CHANGE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition changeResponseConditionDatasource(ResponseConditionDatasourceChangeRequest request) {
        return parameterService.changeResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_CONDITION_DATA_SOURCE_DELETE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition removeResponseConditionDatasource(ResponseConditionDatasourceRemoveRequest request) {
        return parameterService.removeResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_REST_PROVIDER_NAME_LIST")
    @SuppressWarnings("unused")
    public List<RestExternalProviderResponse> getRestProviderNameList() {
        return parameterService.getRestExternalProviderNameList();
    }




}
