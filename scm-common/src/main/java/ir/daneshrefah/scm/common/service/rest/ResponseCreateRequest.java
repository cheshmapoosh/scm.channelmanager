package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import ir.daneshrefah.scm.common.model.service.ExternalServiceBodyType;
import lombok.Data;

@Data
public class ResponseCreateRequest implements RequestData {

    private String transformerId;
    private String serviceId;
    private String serviceProviderId;
    private String errorCode;
    private String errorMessage;
    private ExternalServiceBodyType responseBodyType;

}
