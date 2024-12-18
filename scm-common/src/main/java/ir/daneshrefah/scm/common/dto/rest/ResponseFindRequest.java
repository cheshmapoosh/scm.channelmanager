package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import ir.daneshrefah.scm.common.validation.NotBlankIfPresent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ResponseFindRequest extends PagedRequestData {
    @NotBlankIfPresent
    private String serviceId;
    @NotBlankIfPresent
    private String serviceProviderId;
}
