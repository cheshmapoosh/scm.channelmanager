package ir.daneshrefah.scm.common.dto.error;

import ir.daneshrefah.scm.common.dto.spec.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorMappingSearchRequest extends PagedRequestData {
    private String search;
}
