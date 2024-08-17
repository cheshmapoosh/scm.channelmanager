package ir.daneshrefah.scm.common.service.rest;

import ir.daneshrefah.scm.common.dto.RequestData;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ResponseConditionChangeRequest implements RequestData {

    private Long id;
    private String transformerId;
    private String errorCode;
    private String errorMessage;
    private LocalDateTime lastEditDate;

}
