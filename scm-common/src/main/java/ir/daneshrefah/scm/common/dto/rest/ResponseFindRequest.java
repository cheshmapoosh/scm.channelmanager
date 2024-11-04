package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseFindRequest extends PagedRequestData {
    private String serviceId;
    private String serviceProviderId;
}
