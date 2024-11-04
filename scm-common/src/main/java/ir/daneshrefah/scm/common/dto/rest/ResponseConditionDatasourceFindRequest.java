package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseConditionDatasourceFindRequest implements RequestData {
    private String responseId;
}
