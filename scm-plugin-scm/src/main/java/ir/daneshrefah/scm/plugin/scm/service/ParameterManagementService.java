package ir.daneshrefah.scm.plugin.scm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.daneshrefah.scm.common.dto.rest.*;
import ir.daneshrefah.scm.common.dto.spec.AutoComplete;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.service.rest.*;
import ir.daneshrefah.scm.common.annotation.JavaService;
import ir.daneshrefah.scm.plugin.api.integration.ServiceProducerTemplate;
import ir.daneshrefah.scm.plugin.api.service.AbstractJavaService;
import org.springframework.stereotype.Service;

import java.util.List;

import static ir.daneshrefah.scm.common.constant.ServiceCode.*;

@Service
public class ParameterManagementService extends AbstractJavaService {
    private final ParameterService parameterService;

    public ParameterManagementService(ServiceProducerTemplate producerTemplate, ObjectMapper objectMapper, ParameterService parameterService) {
        super(producerTemplate, objectMapper);
        this.parameterService = parameterService;
    }

    @JavaService(serviceCode = SVC_PARAMETER_CREATE)
    @SuppressWarnings("unused")
    public Parameter createParameter(ParameterCreateRequest request) {
        return parameterService.create(request);
    }

    @JavaService(serviceCode = SVC_PARAMETER_CHANGE)
    @SuppressWarnings("unused")
    public Parameter changeParameter(ParameterChangeRequest request) {
        return parameterService.change(request);
    }

    @JavaService(serviceCode = SVC_PARAMETER_DELETE)
    @SuppressWarnings("unused")
    public Parameter removeParameter(ParameterDeleteRequest request) {
        return parameterService.remove(request);
    }

    @JavaService(serviceCode = SVC_PARAMETER_FIND_BY_ID)
    @SuppressWarnings("unused")
    public Parameter findParameterById(String id) {
        return parameterService.findParameterById(id);
    }

    @JavaService(serviceCode = SVC_PARAMETER_FIND)
    @SuppressWarnings("unused")
    public PagedResponseData<Parameter> findParameter(ParameterFindRequest request) {
        return parameterService.findParameter(request);
    }

    @JavaService(serviceCode = SVC_PARAMETER_FIND_TREE)
    @SuppressWarnings("unused")
    public ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request) {
        return parameterService.findParameterTree(request);
    }

    @JavaService(serviceCode = SVC_PARAMETER_ACTION_TYPE_LIST)
    public List<ParameterActionTypeFindResponse> findParameterActionTypeList(ParameterActionTypeFindRequest request){
        return parameterService.findParameterActionTypeList(request);
    }

    @JavaService(serviceCode = SVC_PARAMETER_AUTO_COMPLETE)
    public PagedResponseData<AutoComplete> searchParameterProviderNameAutoCompleteList(ParameterAutoCompleteSearchRequest request){
        return new PagedResponseData<>(request,parameterService.autoCompleteParameter(request));
    }

}
