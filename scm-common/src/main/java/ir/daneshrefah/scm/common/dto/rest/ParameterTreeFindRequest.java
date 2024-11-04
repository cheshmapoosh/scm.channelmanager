package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

@Data
public class ParameterTreeFindRequest implements RequestData {
    private String actionType;
    private String serviceId;
    private String responseId;
}
