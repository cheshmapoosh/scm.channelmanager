package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;

import java.util.List;

public interface ParameterService {
    Parameter create(ParameterCreateRequest request);

    List<RestExternalProviderResponse> getRestExternalProviderNameList();

    ResponseCondition createResponseCondition(ResponseConditionRequest request);

    ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request);
}
