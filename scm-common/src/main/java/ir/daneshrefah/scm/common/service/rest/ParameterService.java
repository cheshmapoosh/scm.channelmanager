package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.rest.*;
import ir.daneshrefah.scm.common.dto.spec.AutoComplete;
import ir.daneshrefah.scm.common.dto.spec.PagedResponseData;
import ir.daneshrefah.scm.common.model.service.parameter.Parameter;

import java.util.List;

public interface ParameterService {
    Parameter create(ParameterCreateRequest request);

    Parameter remove(ParameterDeleteRequest request);

    Parameter change(ParameterChangeRequest request);

    Parameter findParameterById(String id);

    PagedResponseData<Parameter> findParameter(ParameterFindRequest request);

    ParameterTreeFindResponse findParameterTree(ParameterTreeFindRequest request);

    List<ParameterActionTypeFindResponse> findParameterActionTypeList(ParameterActionTypeFindRequest request);

    List<AutoComplete> autoCompleteParameter(ParameterAutoCompleteSearchRequest request);

}
