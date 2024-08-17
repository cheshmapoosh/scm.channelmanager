package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.ResponseCondition;

import java.util.List;

public interface ParameterService {
    Parameter create(ParameterCreateRequest request);

    List<RestExternalProviderResponse> getRestExternalProviderNameList();

    ResponseCondition createResponseCondition(ResponseConditionRequest request);

    ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request);

    Parameter change(ParameterChangeRequest request);

    ResponseCondition changeResponseCondition(ResponseConditionChangeRequest request);

    ParameterDatasourceCondition changeResponseConditionDatasource(ResponseConditionDatasourceChangeRequest request);

    Parameter remove(ParameterDeleteRequest request);

    ResponseCondition removeResponseCondition(ResponseConditionDeleteRequest request);

    ParameterDatasourceCondition removeResponseConditionDatasource(ResponseConditionDatasourceRemoveRequest request);

    Parameter findParameterById(String id);

    PagedResponseData<Parameter> findParameter(ParameterFindRequest request);

    ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request);

    PagedResponseData<ResponseCondition> findResponseCondition(ResponseConditionFindRequest request);
}
