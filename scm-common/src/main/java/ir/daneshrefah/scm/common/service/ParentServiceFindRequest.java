package ir.daneshrefah.scm.common.service;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ParentServiceFindRequest extends PagedRequestData {
    private String search;
}
