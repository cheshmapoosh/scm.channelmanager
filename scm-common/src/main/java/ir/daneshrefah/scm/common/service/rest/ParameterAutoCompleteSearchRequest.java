package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.constant.ParameterAutoCompleteProperty;
import ir.daneshrefah.scm.common.constant.ParameterTarget;
import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParameterAutoCompleteSearchRequest extends PagedRequestData {
    private String targetType;
    private String propertyName;
    private String parameterActionType;
    private String search;
}
