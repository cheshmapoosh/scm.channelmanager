package ir.daneshrefah.scm.common.service.error;

import ir.daneshrefah.scm.common.dto.PagedRequestData;
import ir.daneshrefah.scm.common.model.message.MessageStatus;
import lombok.Data;

@Data
public class ErrorMappingFindRequest extends PagedRequestData {
    private String providerId;
    private String providerErrorCode;
    private String exceptionClassName;
    private Integer scmErrorCode;
    private String exceptionOverrideName;
    private MessageStatus status;
}
