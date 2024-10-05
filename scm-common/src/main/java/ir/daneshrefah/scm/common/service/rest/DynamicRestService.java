package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.ParameterDatasourceCondition;
import ir.daneshrefah.scm.common.model.service.parameter.Response;

import java.util.List;

public interface DynamicRestService {
    Response createResponse(ResponseCreateRequest request);

    List<RestExternalProviderResponse> getRestExternalProviderNameList();

    ParameterDatasourceCondition createResponseConditionDatasource(ResponseConditionDatasourceRequest request);

    Response changeResponse(ResponseChangeRequest request);

    ParameterDatasourceCondition changeResponseConditionDatasource(ResponseConditionDatasourceChangeRequest request);

    Response removeResponse(ResponseDeleteRequest request);

    ParameterDatasourceCondition removeResponseConditionDatasource(ResponseConditionDatasourceRemoveRequest request);

    PagedResponseData<Response> findResponse(ResponseFindRequest request);

    List<ParameterDatasourceCondition> findResponseConditionDatasourceList(ResponseConditionDatasourceFindRequest request);
}
