package ir.daneshrefah.scm.common.service.error;

import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ErrorMappingEditRequest  {
    private String id;
    private String providerId;
    private String providerErrorCode;
    private Integer scmErrorCode;
    private String exceptionOverrideName;
    private MessageStatus status;
    private LocalDateTime lastEditDate;
}
