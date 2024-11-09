package ir.daneshrefah.scm.common.dto.rest;

import ir.daneshrefah.scm.common.dto.spec.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseChangeRequest implements RequestData {

    private String id;
    private String transformerId;
    private String errorCode;
    private String errorMessage;
    private String responseBodyType;
    private LocalDateTime lastEditDate;
    private Boolean enable;
    private String title;

}
