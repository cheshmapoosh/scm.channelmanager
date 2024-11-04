package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
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
