package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    @JavaService(serviceCode = "SVC_RESPONSE_CONDITION_CREATE")
    @SuppressWarnings("unused")
    public ResponseCondition createResponseCondition(ResponseConditionRequest request) {
        return parameterService.createResponseCondition(request);
    }

    @JavaService(serviceCode = "SVC_CONDITION_DATA_SOURCE_CREATE")
    @SuppressWarnings("unused")
    public ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request) {
        return parameterService.createResponseConditionDatasource(request);
    }

    @JavaService(serviceCode = "SVC_REST_PROVIDER_NAME_LIST")
    @SuppressWarnings("unused")
    public List<RestExternalProviderResponse> getRestProviderNameList() {
        return parameterService.getRestExternalProviderNameList();
    }




}
