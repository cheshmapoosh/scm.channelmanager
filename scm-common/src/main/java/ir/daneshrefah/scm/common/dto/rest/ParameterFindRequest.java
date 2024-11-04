package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParameterFindRequest extends PagedRequestData {
    private String parameterName;
    private String parentId;
    private String actionType;
    private String title;
    //Parameter target usage
    private String serviceProviderId;
    private String serviceId;
    private String responseId;
}
