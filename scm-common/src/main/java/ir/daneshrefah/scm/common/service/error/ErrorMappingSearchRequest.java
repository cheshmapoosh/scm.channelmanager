package ir.daneshrefah.scm.common.service.error;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ErrorMappingSearchRequest extends PagedRequestData {
    private String search;
}
