package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;

public interface ParameterService {
    Parameter create(ParameterCreateRequest request);

    Parameter remove(ParameterDeleteRequest request);

    Parameter change(ParameterChangeRequest request);

    Parameter findParameterById(String id);

    PagedResponseData<Parameter> findParameter(ParameterFindRequest request);

    ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request);

}
