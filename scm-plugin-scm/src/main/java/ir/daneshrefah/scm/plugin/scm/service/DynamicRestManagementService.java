package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.Response;
import ir.daneshrefah.scm.common.service.rest.*;
import ir.daneshrefah.scm.plugin.api.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DynamicRestManagementService extends AbstractJavaService {

    private final DynamicRestService dynamicRestService;
    public DynamicRestManagementService
            (
                    ServiceProducerTemplate producerTemplate,
                    ObjectMapper objectMapper,
                    DynamicRestService dynamicRestService
            ) {
        super(producerTemplate, objectMapper);
        this.dynamicRestService = dynamicRestService;
    }


    @JavaService(serviceCode = "SVC_RESPONSE_FIND")
    @SuppressWarnings("unused")
    public PagedResponseData<Response> findResponse(ResponseFindRequest request) {
        return dynamicRestService.findResponse(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CREATE")
    @SuppressWarnings("unused")
    public Response createResponse(ResponseCreateRequest request) {
        return dynamicRestService.createResponse(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CHANGE")
    @SuppressWarnings("unused")
    public Response changeResponse(ResponseChangeRequest request) {
        return dynamicRestService.changeResponse(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_DELETE")
    @SuppressWarnings("unused")
    public Response removeResponse(ResponseDeleteRequest request) {
        return dynamicRestService.removeResponse(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_DATA_SOURCE_CREATE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request) {
        return dynamicRestService.createResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_DATA_SOURCE_CHANGE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition changeResponseConditionDatasource(ResponseConditionDatasourceChangeRequest request) {
        return dynamicRestService.changeResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_DATA_SOURCE_DELETE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition removeResponseConditionDatasource(ResponseConditionDatasourceRemoveRequest request) {
        return dynamicRestService.removeResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_REST_PROVIDER_NAME_LIST")
    @SuppressWarnings("unused")
    public List<RestExternalProviderResponse> getRestProviderNameList() {
        return dynamicRestService.getRestExternalProviderNameList();
    }


    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_DATA_SOURCE_LIST")
    @SuppressWarnings("unused")
    public List<ParameterDatasourceCondition> findResponseConditionDatasourceList(ResponseConditionDatasourceFindRequest request) {
        return dynamicRestService.findResponseConditionDatasourceList(request);
    }


}
