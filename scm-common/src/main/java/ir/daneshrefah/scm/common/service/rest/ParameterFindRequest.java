package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
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
