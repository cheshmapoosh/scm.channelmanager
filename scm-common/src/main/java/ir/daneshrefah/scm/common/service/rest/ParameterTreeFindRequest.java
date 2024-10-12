package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

@Data
public class ParameterTreeFindRequest implements RequestData {
    private String actionType;
    private String serviceId;
    private String responseId;
}
