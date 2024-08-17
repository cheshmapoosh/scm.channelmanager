package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseConditionFindRequest extends PagedRequestData {
    private String serviceId;
    private String providerId;
}
