package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseConditionDatasourceFindRequest implements RequestData {
    private Long responseConditionId;
}
