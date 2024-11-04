package ir.daneshrefah.scm.common.dto;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParentServiceFindRequest extends PagedRequestData {
    private String search;
}
