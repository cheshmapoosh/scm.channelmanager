package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
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
    private Boolean enable;
    private String title;

}
